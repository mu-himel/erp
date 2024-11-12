package com.agi.aesl.erpscm.price_quotation.service;

import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import com.agi.aesl.erpscm.fileupload.service.FileUploadService;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.indent.service.IndentService;
import com.agi.aesl.erpscm.integration.tender.TenderService;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.price_quotation.dto.request.*;
import com.agi.aesl.erpscm.price_quotation.entity.*;
import com.agi.aesl.erpscm.price_quotation.enums.CreditType;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStateStatus;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStatus;
import com.agi.aesl.erpscm.price_quotation.repository.PqRepository;
import com.agi.aesl.erpscm.price_quotation.repository.PqSummaryRepository;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PqServiceImpl implements PqService{

    @Autowired
    private IndentService indentService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private TenderService tenderService;

    @Autowired
    private PqRepository pqRepository;

    @Autowired
    private PqSummaryRepository pqSummaryRepository;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private CpsServerConfig cpsConfig;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private OrgService orgService;

    @Autowired
    private FileUploadService fileUploadService;

    @Override
    @Transactional
    public void onReceivePq(Jwt token, PriceQuotationReqDto pqDto) {
        claimResolver.setToken(token);
        Optional<Indent> indentOp = indentService.getIndentFactory(pqDto);
        this._savePq(indentOp,pqDto,PriceQuotationStatus.INIT,PriceQuotationStateStatus.RECEIVED);
    }

    @Override
    @Transactional
    public void onDeclinePq(Jwt token, Long id, NoteDto noteDto, PriceQuotationStateStatus status, PriceQuotationStatus actionFrom) {
        claimResolver.setToken(token);
        if(noteDto.getNote()==null || noteDto.getNote().isEmpty()){
            throw new RuntimeException("Sorry! Decline Note Required");
        }
        Optional<PriceQuotation> pqOptional =
                ((actionFrom.equals(PriceQuotationStatus.COUNTER_TO_COMPANY))?
                pqRepository.findByRemoteOfferId(id): pqRepository.findById(id));
        if(pqOptional.isPresent()){
            PriceQuotation priceQuotation = pqOptional.get();
            priceQuotation.setStatus(status);
            priceQuotation.setDeclinedMessage(noteDto.getNote());
            if((actionFrom.equals(PriceQuotationStatus.COUNTER_TO_VENDOR))){
                setStatus(id,PriceQuotationStateStatus.DECLINED,null,noteDto);
            }
        }
    }

    @Transactional
    private void _savePq(Optional<Indent> indentOp, PriceQuotationReqDto pqDto,
                         PriceQuotationStatus priceQuotationStatus,
                         PriceQuotationStateStatus priceQuotationStateStatus){
        if(indentOp.isEmpty()){
            throw new RuntimeException("Sorry! indent not found");
        }
        Indent indent = indentOp.get();
        PriceQuotation pq = new PriceQuotation();
        pq.setRfq(indent);

        if(pqDto.getFile()!=null && !pqDto.getFile().isEmpty()){
            pq.setFile(pqDto.getFile());
        }

        pq.setNegotiationHistoryId(pqDto.getNegotiationHistoryId());
        pq.setRemoteOfferId(pqDto.getRemoteOfferId());
        if(pqDto.getIsFinal()){
            pq.setIsFinal(pqDto.getIsFinal());
        }else{
            pq.setIsFinal(false);
        }

        pq.setVendorId(pqDto.getVendorId());
        pq.setVendorName(pqDto.getVendorName());
        pq.setVendorEmail(pqDto.getVendorEmail());
        pq.setVendorPhoneNo(pqDto.getVendorPhoneNo());
        pq.setVendorType(pqDto.getVendorType());
        if(pqDto.getTermsAndConditions().size()>0) {
            pq.setTermsAndConditions(pqDto.getTermsAndConditions().stream().map(tnc -> {
                PqTermsAndCondition pqTermsAndCondition = new PqTermsAndCondition();
                pqTermsAndCondition.setPriceQuotation(pq);
                pqTermsAndCondition.setTermAndCondition(tnc);
                pqTermsAndCondition.setVendorId(pqDto.getVendorId());
                pqTermsAndCondition.setRfq(indent);
                return pqTermsAndCondition;
            }).collect(Collectors.toList()));
        }
        if(priceQuotationStateStatus.equals(PriceQuotationStateStatus.SENT)){
            Optional<PqRepository.PriceQuotationInfo> pqOp = pqRepository.getPrevPqByVendorId(pq.getVendorId());
            if(pqOp.isPresent()){
                pq.setScore(pqOp.get().getScore());
            }
        }else{
            pq.setScore(pqDto.getScore());
        }
        pq.setPaymentMethod(pqDto.getPaymentMethod().toString());
        pq.setPriceQuotationStatus(priceQuotationStatus);
        pq.setStatus(priceQuotationStateStatus);
        pq.setQuotationDetails(pqDto.getDetails().stream().map(detail->{
            PriceQuotationDetail pqd = new PriceQuotationDetail();
            pqd.setWarrantyDuration(detail.getWarrantyDuration());
            pqd.setWarrantyUnit(detail.getWarrantyUnit());
            pqd.setPriceQuotation(pq);
            pqd.setBrandName(detail.getBrandName());
            pqd.setItemAttribute(detail.getItemAttributeName());
            pqd.setExtendedAttributes(detail.getExtendedAttributes());
            pqd.setRfqQty(detail.getRfqQty());
            pqd.setUnitPrice(detail.getUnitPrice());
            pqd.setTotalPrice(pqd.getUnitPrice().multiply(BigDecimal.valueOf(detail.getRfqQty())));
            pqd.setEstDeliveryDays(detail.getEstDeliveryDays());
            pqd.setDeliveryDetails(detail.getDeliveryDetails().stream().map(_pqdd->{
                PriceQuotationDeliveryDetail pqdd = new PriceQuotationDeliveryDetail();
                pqdd.setDeliveryCharge(_pqdd.getDeliveryChargeType());
                pqdd.setDeliveryChargeAmount(_pqdd.getDeliveryChargeAmount());
                Optional<Warehouse> warehouseOp = warehouseService.getWarehouse(_pqdd.getWarehouseId());
                if(warehouseOp.isEmpty()){
                    throw new RuntimeException("Warehouse not found");
                }
                if(priceQuotationStateStatus.equals(PriceQuotationStateStatus.SENT)){
                    Optional<DeliveryDetailDto> deliveryDetailDtoOp = pqDto.getWarehouses().stream().filter(w->{
                       return w.getWarehouseId().equals(_pqdd.getWarehouseId());
                    }).findFirst();
                    if(deliveryDetailDtoOp.isPresent()){
                        Optional<ItemDeliveryDetailDto> itemOp = deliveryDetailDtoOp.get().getItems().stream().filter(itemDeliveryDetailDto -> {
                           return itemDeliveryDetailDto.getItemName().equals(detail.getItemAttributeName());
                        }).findFirst();
                        if(itemOp.isPresent()){
                            pqdd.setDeliveryOrderQty(itemOp.get().getDeliveryOrderQty());
                        }

                    }
                }else {
                    pqdd.setDeliveryOrderQty(_pqdd.getDeliveryOrderQty());
                }
                pqdd.setWarehouse(warehouseOp.get());
                pqdd.setPriceQuotationDetail(pqd);
                return pqdd;
            }).collect(Collectors.toList()));

            return pqd;
        }).collect(Collectors.toList()));

        pqRepository.save(pq);

        this._savePriceQuotationSummary(pq,pqDto);

        if(priceQuotationStateStatus.equals(PriceQuotationStateStatus.SENT)){
            CounterPqDto offerRequestDto = _preparePayloadForSentCounterOffer(pqDto);
            Optional<Long> remoteOfferIdOp = tenderService.sentCounterOffer(claimResolver, indent, offerRequestDto);
            pq.setRemoteOfferId(remoteOfferIdOp.orElse(null));
        }
    }

    @Transactional
    private void _savePriceQuotationSummary(PriceQuotation priceQuotation, PriceQuotationReqDto pqDto){
        PriceQuotationSummary pqs = new PriceQuotationSummary();
        pqs.setMushakIncluded(pqDto.getPriceQuotationSummary().getMushak());
        pqs.setDeliveryCharge(pqDto.getPriceQuotationSummary().getDeliveryCharge());
        pqs.setDeliveryChargeAmount(pqDto.getPriceQuotationSummary().getDeliveryChargeAmount());
        pqs.setCreditPaymentUnit(pqDto.getPriceQuotationSummary().getCreditPaymentUnit());
        pqs.setCreditPaymentDuration(pqDto.getPriceQuotationSummary().getCreditPaymentDuration());
        pqs.setIsAitAdded(pqDto.getPriceQuotationSummary().getIsAitAdded());
        pqs.setIsVatAdded(pqDto.getPriceQuotationSummary().getIsVatAdded());
        pqs.setNote(pqDto.getPriceQuotationSummary().getNote());
        if(pqDto.getPriceQuotationSummary()!=null && pqDto.getPriceQuotationSummary().getVatAmount() !=null){
            if(pqDto.getPriceQuotationSummary().getVatAmount().contains(".")){
                pqs.setVatAmount(BigDecimal.valueOf(Double.parseDouble(pqDto.getPriceQuotationSummary().getVatAmount())));
            }else{
                pqs.setVatAmount(BigDecimal.valueOf(Long.parseLong(pqDto.getPriceQuotationSummary().getVatAmount())));
            }

        }
        if(pqDto.getPriceQuotationSummary()!=null && pqDto.getPriceQuotationSummary().getVatPercent() !=null){
            pqs.setVatPercent(BigDecimal.valueOf(Long.parseLong(pqDto.getPriceQuotationSummary().getVatPercent())));
        }

        pqs.setSubTotalPrice(pqDto.getPriceQuotationSummary().getSubTotalPrice());
        pqs.setTotalPrice(pqDto.getPriceQuotationSummary().getTotalPrice());
        pqs.setPriceQuotation(priceQuotation);
        pqSummaryRepository.save(pqs);
    }

    private CounterPqDto _preparePayloadForSentCounterOffer(PriceQuotationReqDto pqDto, PriceQuotation pq, PriceQuotationSummary pqs){
        CounterPqDto offerRequestDto = new CounterPqDto();
        offerRequestDto.setCreditPaymentDays(pqs.getCreditPaymentDuration());
        offerRequestDto.setCreditType(CreditType.valueOf(pq.getPaymentMethod()));
        offerRequestDto.setVatIncluded(pqs.getIsVatAdded());
        offerRequestDto.setAitIncluded(pqs.getIsAitAdded());
        offerRequestDto.setVatPercent(pqs.getVatPercent());
//        if(pqs.getVatAmount().contains(".")){
//            offerRequestDto.setVatAmount(BigDecimal.valueOf(Double.parseDouble(pqDto.getPriceQuotationSummary().getVatAmount())));
//        }else{
//            offerRequestDto.setVatAmount(BigDecimal.valueOf(Long.parseLong(pqDto.getPriceQuotationSummary().getVatAmount())));
//        }
        offerRequestDto.setVatAmount(pqs.getVatAmount());

        offerRequestDto.setIsFinal(pq.getIsFinal());
        offerRequestDto.setNegotiationHistoryId(pq.getNegotiationHistoryId());
        if(pqs.getMushakIncluded()!=null){
            offerRequestDto.setMushakIncluded(true);
        }else{
            offerRequestDto.setMushakIncluded(false);
        }
        offerRequestDto.setFinalOfferPrice(pqs.getSubTotalPrice());
        offerRequestDto.setTotalDeliveryChargeAmount(pqs.getDeliveryChargeAmount());

        offerRequestDto.setNote(pqs.getNote());
        List<CounterItemDto> offerItems = new ArrayList<>();


        pq.getQuotationDetails().stream().forEach(d->{
            CounterItemDto offerItemDto = new CounterItemDto();
            offerItemDto.setWarrantyDuration(d.getWarrantyDuration());
            offerItemDto.setWarrantyUnit(d.getWarrantyUnit());
            offerItemDto.setEstimatedDeliveryDays(Long.valueOf(d.getEstDeliveryDays()));
            offerItemDto.setItemQuantity(d.getRfqQty());
            String desc = (d.getBrandName()!=null)? d.getBrandName() + "-"+ d.getItemAttribute() : d.getItemAttribute();
            offerItemDto.setProductDescription(desc);
            offerItemDto.setSpecification("Must be a good condition");
            CounterPriceQuotation opq = new CounterPriceQuotation();
            opq.setTotalPrice(d.getUnitPrice().multiply(BigDecimal.valueOf(d.getRfqQty())));
            opq.setPricePerUnit(d.getUnitPrice());
            offerItemDto.setPriceQuotation(opq);
            offerItems.add(offerItemDto);

        });
        offerRequestDto.setTermsAndConditions(pq.getTermsAndConditions()
                .stream().map(ta-> new CounterTermAndConditionDto(ta.getTermAndCondition())).collect(Collectors.toList())
        );
        offerRequestDto.setOfferItems(offerItems);
        offerRequestDto.setWarehouses(pqDto.getWarehouses().stream().map(pd->{
            DeliveryDetailDto odd = new DeliveryDetailDto();
            odd.setDeliveryChargeAmount(pd.getDeliveryChargeAmount());
            odd.setDeliveryChargeMode(pd.getDeliveryChargeMode().toString());
            odd.setWarehouseId(pd.getWarehouseId());
            odd.setItems(pd.getItems().stream().map(pdid->{
                ItemDeliveryDetailDto oidd= new ItemDeliveryDetailDto();
                oidd.setDeliveryOrderQty(pdid.getDeliveryOrderQty());
                oidd.setItemName(pdid.getItemName());
                return oidd;
            }).collect(Collectors.toList()));
            return odd;
        }).collect(Collectors.toList()));
        return offerRequestDto;
    }
    private CounterPqDto _preparePayloadForSentCounterOffer(PriceQuotationReqDto pqDto){
        CounterPqDto offerRequestDto = new CounterPqDto();
        offerRequestDto.setCreditPaymentDays(pqDto.getPriceQuotationSummary().getCreditPaymentDuration());
        offerRequestDto.setCreditType(pqDto.getPaymentMethod());
        offerRequestDto.setVatIncluded(pqDto.getPriceQuotationSummary().getIsVatAdded());
        offerRequestDto.setAitIncluded(pqDto.getPriceQuotationSummary().getIsAitAdded());
        offerRequestDto.setVatPercent(BigDecimal.valueOf(Long.parseLong(pqDto.getPriceQuotationSummary().getVatPercent())));
        if(pqDto.getPriceQuotationSummary().getVatAmount().contains(".")){
            offerRequestDto.setVatAmount(BigDecimal.valueOf(Double.parseDouble(pqDto.getPriceQuotationSummary().getVatAmount())));
        }else{
            offerRequestDto.setVatAmount(BigDecimal.valueOf(Long.parseLong(pqDto.getPriceQuotationSummary().getVatAmount())));
        }

        offerRequestDto.setIsFinal(pqDto.getIsFinal());
        offerRequestDto.setNegotiationHistoryId(pqDto.getNegotiationHistoryId());
        if(pqDto.getPriceQuotationSummary().getMushak()!=null){
            offerRequestDto.setMushakIncluded(true);
        }else{
            offerRequestDto.setMushakIncluded(false);
        }
        offerRequestDto.setFinalOfferPrice(pqDto.getPriceQuotationSummary().getSubTotalPrice());
        offerRequestDto.setTotalDeliveryChargeAmount(pqDto.getPriceQuotationSummary().getDeliveryChargeAmount());

        offerRequestDto.setNote(pqDto.getPriceQuotationSummary().getNote());
        List<CounterItemDto> offerItems = new ArrayList<>();


        pqDto.getDetails().stream().forEach(d->{
            CounterItemDto offerItemDto = new CounterItemDto();
            offerItemDto.setWarrantyDuration(d.getWarrantyDuration());
            offerItemDto.setWarrantyUnit(d.getWarrantyUnit());
            offerItemDto.setEstimatedDeliveryDays(Long.valueOf(d.getEstDeliveryDays()));
            offerItemDto.setItemQuantity(d.getRfqQty());
            offerItemDto.setBrandName(d.getBrandName());
            offerItemDto.setProductDescription(d.getItemAttributeName());
            offerItemDto.setSpecification("Must be a good condition");
            CounterPriceQuotation opq = new CounterPriceQuotation();
            opq.setTotalPrice(d.getUnitPrice().multiply(BigDecimal.valueOf(d.getRfqQty())));
            opq.setPricePerUnit(d.getUnitPrice());
            offerItemDto.setPriceQuotation(opq);
            offerItems.add(offerItemDto);

        });
        offerRequestDto.setTermsAndConditions(pqDto.getTermsAndConditions()
                .stream().map(CounterTermAndConditionDto::new).collect(Collectors.toList())
        );
        offerRequestDto.setOfferItems(offerItems);
        offerRequestDto.setWarehouses(pqDto.getWarehouses().stream().map(pd->{
            DeliveryDetailDto odd = new DeliveryDetailDto();
            odd.setDeliveryChargeAmount(pd.getDeliveryChargeAmount());
            odd.setDeliveryChargeMode(pd.getDeliveryChargeMode().toString());
            odd.setWarehouseId(pd.getWarehouseId());
            odd.setItems(pd.getItems().stream().map(pdid->{
                ItemDeliveryDetailDto oidd= new ItemDeliveryDetailDto();
                oidd.setDeliveryOrderQty(pdid.getDeliveryOrderQty());
                oidd.setItemName(pdid.getItemName());
                return oidd;
            }).collect(Collectors.toList()));
            return odd;
        }).collect(Collectors.toList()));
        return offerRequestDto;
    }

    @Override
    public List<?> getPriceQuotationsByIndent(Long id) {
        return pqRepository.getPriceQuotationsByIndentId(id);
    }
    @Override
    public Optional<?> getDetail(Long id) {
        Optional<PriceQuotation> pqOptional = pqRepository.findById(id);
        if(pqOptional.isEmpty()){
            throw new RuntimeException("Sorry! Price quotation not found by this id");
        }
        Map<String,Object> result = new HashMap<>();
        PriceQuotation pq = pqOptional.get();

        Optional<PriceQuotationSummary> sOptional = pqSummaryRepository.findByPriceQuotationId(pq.getId());

        result.put("vendorId",pq.getVendorId());
        result.put("vendorEmail",pq.getVendorEmail());
        result.put("vendorType",pq.getVendorType());
        result.put("vendorPhoneNo",pq.getVendorPhoneNo());
        result.put("vendorName", pq.getVendorName());
        result.put("dateTime",pq.getPriceQuotationDate());
        result.put("paymentMethod",pq.getPaymentMethod());
        result.put("negotiationHistoryId",pq.getNegotiationHistoryId());
        result.put("declineMessage",pq.getDeclinedMessage());
        result.put("isRecommendedForCs",pq.getIsRecommendForCs());
        List<String> termsAndConditions = pq.getTermsAndConditions().stream().filter(
                tnc->tnc.getVendorId().equals(pq.getVendorId())
        ).map(tnc->tnc.getTermAndCondition()).collect(Collectors.toList());
        result.put("termsAndConditions",termsAndConditions);
        //TODO Fetch Terms and conditions

        if(sOptional.isPresent()){
            PriceQuotationSummary pqs = sOptional.get();
            pqs.setPriceQuotation(null);
            result.put("pqSummary",pqs);
        }

        List<Map<String,Object>> details = new ArrayList<>();
        pq.getQuotationDetails().stream().forEach(pqd->{
            Map<String,Object> itemDef = new HashMap<>();
            itemDef.put("itemAttribute",pqd.getItemAttribute());
            itemDef.put("brandName", pqd.getBrandName());
            itemDef.put("extendedAttributes",pqd.getExtendedAttributes());
            itemDef.put("estimatedDeliveryDays",pqd.getEstDeliveryDays());
            itemDef.put("warrantyDuration",pqd.getWarrantyDuration());
            itemDef.put("warrantyUnit",pqd.getWarrantyUnit());
            itemDef.put("rfqQty",pqd.getRfqQty());
            itemDef.put("unitPrice",pqd.getUnitPrice());
            itemDef.put("totalPrice",pqd.getTotalPrice());
            List<Map<String,Object>> warehouses = new ArrayList<>();
            pqd.getDeliveryDetails().stream().forEach(pqdd->{
                Map<String,Object> itemWDef = new HashMap<>();
                itemWDef.put("warehouseId",pqdd.getWarehouse().getId());
                itemWDef.put("warehouseName",pqdd.getWarehouse().getName());
                itemWDef.put("warehouseLocation",pqdd.getWarehouse().getLocation());
                itemWDef.put("deliveryCharge", pqdd.getDeliveryCharge());
                itemWDef.put("deliveryChargeAmount", pqdd.getDeliveryChargeAmount());
                itemWDef.put("deliveryOrderQty",pqdd.getDeliveryOrderQty());
                warehouses.add(itemWDef);
            });
            itemDef.put("warehouses",warehouses);
            details.add(itemDef);
        });

        result.put("details",details);
        return Optional.ofNullable(result);
    }

    @Override
    public List<?> getHistoriesByRfq(Long id, Long vendorId) {
        List<PriceQuotation> priceQuotations = pqRepository.findByRfqIdAndVendorId(id,vendorId);

        record NegotiationHistory(Long id, String title){ };

        List<NegotiationHistory> histories = priceQuotations.stream().map(priceQuotation->{
            StringBuilder sb = new StringBuilder();
            if(priceQuotation.getIsFinal()!=null && priceQuotation.getIsFinal().equals(true)){
                sb.append("Final ");
            }
            if(priceQuotation.getPriceQuotationStatus().equals(PriceQuotationStatus.INIT)){
                sb.append("Initial Quotation Received From "+priceQuotation.getVendorName());
            }
            if(priceQuotation.getPriceQuotationStatus().equals(PriceQuotationStatus.COUNTER_TO_VENDOR)){
                sb.append("Counter Offer to "+priceQuotation.getVendorName());
            }
            if(priceQuotation.getPriceQuotationStatus().equals(PriceQuotationStatus.COUNTER_TO_COMPANY)){
                sb.append("Counter Offer Received from "+priceQuotation.getVendorName());
            }
            return new NegotiationHistory(priceQuotation.getId(), sb.toString());
        }).collect(Collectors.toList());

        return histories;
    }

    @Override
    @Transactional
    public void lockPq(Jwt token, Long id, PriceQuotationStateStatus status) {
        claimResolver.setToken(token);
        setStatus(id, status,null,null);
    }
    @FunctionalInterface
    private interface Recommendable {
        public void setIsRecommendForCs(PriceQuotation p);
    }
    @Transactional
    private void setStatus(Long id, PriceQuotationStateStatus status, Recommendable recommendable,
                           NoteDto noteDto) {
        Optional<PriceQuotation> pqOptional = pqRepository.findById(id);
        if(pqOptional.isEmpty()){
            throw new RuntimeException("Sorry! Price Quotation not found");
        }

        PriceQuotation priceQuotation = pqOptional.get();

        if(recommendable!=null){

            recommendable.setIsRecommendForCs(priceQuotation);
        }else{
            priceQuotation.setStatus(status);
        }
        if(status.equals(PriceQuotationStateStatus.DECLINED)){
            priceQuotation.setDeclinedMessage(noteDto.getNote());
        }

        setRemoteOfferStatus(status, priceQuotation.getRemoteOfferId(),priceQuotation.getVendorId(),noteDto);


    }

    @Transactional
    private void setRemoteOfferStatus(PriceQuotationStateStatus status,Long remoteOfferId, Long venodrId, NoteDto noteDto){
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
        if(orgOp.isPresent()){
            headers.set("orgId",orgOp.get().getCpsVendorRegistrationId().toString());
        }

        ResponseEntity<?> response=null;
        String url = "";
        if(status.equals(PriceQuotationStateStatus.LOCKED)){
            HttpEntity<Void> payload = new HttpEntity<>(headers);
            url = cpsConfig.getLockOfferEndpoint(remoteOfferId, venodrId);
            response = networkService.put(url, payload, Void.class);
        }
        if(status.equals(PriceQuotationStateStatus.DECLINED)){
            HttpEntity<NoteDto> payload = new HttpEntity<>(noteDto,headers);
            url = cpsConfig.getDeclineOfferEndpoint(remoteOfferId,venodrId);
            System.out.println(url);
            response = networkService.put(url, payload, Void.class);
        }
        if(status.equals(PriceQuotationStateStatus.AWARDED)){
            HttpEntity<Void> payload = new HttpEntity<>(headers);
            url = cpsConfig.getAwardedOfferEndpoint(remoteOfferId,venodrId);
            response = networkService.put(url, payload, Void.class);
        }

        if(response!=null && response.getStatusCode()!= HttpStatus.NO_CONTENT){
            throw new RuntimeException("Unable to send Offer to CPS");
        }
    }

    @Override
    @Transactional
    public void sendPq(Jwt token, PriceQuotationReqDto pqDto) {
        claimResolver.setToken(token);
        Optional<Indent> indentOp = indentService.getIndentFactory(pqDto);
        this._savePq(indentOp,pqDto,PriceQuotationStatus.COUNTER_TO_VENDOR,PriceQuotationStateStatus.SENT);
    }

    @Override
    @Transactional
    public void addManualPq(Jwt token, PriceQuotationReqDto pqDto) {
        claimResolver.setToken(token);
        Optional<Indent> indentOp = indentService.getIndentFactory(pqDto);
        this._savePq(indentOp,pqDto,PriceQuotationStatus.INIT,PriceQuotationStateStatus.RECEIVED);
    }

    @Override
    @Transactional
    public void recommendPq(Jwt token, Long id) {
        claimResolver.setToken(token);

        setStatus(id,PriceQuotationStateStatus.AWARDED,(pq)->{
            pq.setIsRecommendForCs(true);
            pq.setStatus(PriceQuotationStateStatus.LOCKED);

        },null);
    }

    @Override
    @Transactional
    public void onLockPq(Jwt token, Long id) {
        claimResolver.setToken(token);
        Optional<PriceQuotation> pqOp = pqRepository.findByRemoteOfferId(id);
        if(pqOp.isEmpty()){
            throw new RuntimeException("Sorry! Price Quotation not exist");
        }
        PriceQuotation priceQuotation = pqOp.get();
        priceQuotation.setStatus(PriceQuotationStateStatus.LOCKED);
//        priceQuotation.setPriceQuotationStatus(PriceQuotationStatus.COUNTER_TO_COMPANY);
    }

    @Override
    @Transactional
    public void onReceiveCounterPq(Jwt token, PriceQuotationReqDto pqDto) {
        Optional<Indent> indentOp = indentService.getIndentFactory(pqDto);
        this._savePq(indentOp,pqDto,PriceQuotationStatus.COUNTER_TO_COMPANY,PriceQuotationStateStatus.RECEIVED);
    }

    @Override
    public FileUploadResponse uploadDoc(Long rfqId, MultipartFile file) {
        if(!fileUploadService.checkMimeType(file.getContentType(),
                "application/pdf",
                "image/jpeg","image/png",
                "application/vnd.oasis.opendocument.text",
                "application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/csv")){
            throw new RuntimeException("Sorry! not file not a valid type (pdf,odt,doc,docx,xls,xlsx,csv)");
        }
        Path path = Path.of("./upload/"+rfqId+"/pq/", file.getOriginalFilename());
        return fileUploadService.uploadFile(path, file);
    }
}
