package com.agi.aesl.erpscm.price_quotation.service;

import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.exception.AesException;
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
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStateStatus;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStatus;
import com.agi.aesl.erpscm.price_quotation.repository.PqRepository;
import com.agi.aesl.erpscm.price_quotation.repository.PqSummaryRepository;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
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

    @Value("${upload.dir}")
    private String uploadDir;

    private static final String DIR_SEPARATOR="/";

    @Override
    @Transactional
    public void onReceivePq(Jwt token, PriceQuotationReqDto pqDto) {
        claimResolver.setToken(token);
        Optional<Indent> indentOp = indentService.getIndentFactory(pqDto);
        this.savePq(indentOp,pqDto,PriceQuotationStatus.INIT,PriceQuotationStateStatus.RECEIVED);
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
    private Long savePq(Optional<Indent> indentOp, PriceQuotationReqDto pqDto,
                         PriceQuotationStatus priceQuotationStatus,
                         PriceQuotationStateStatus priceQuotationStateStatus){
        if(indentOp.isEmpty()){
            throw new AesException("Sorry! indent not found");
        }
        Indent indent = indentOp.get();
        if(indent.getExpireDateTime().isBefore(LocalDateTime.now())){
            throw new AesException("Sorry! Tender submission time has been expired");
        }

        PriceQuotation pq = new PriceQuotation();
        pq.setRfq(indent);

        if(pqDto.getFile()!=null && !pqDto.getFile().isEmpty()){
            pq.setFile(pqDto.getFile());
        }

        pq.setNegotiationHistoryId(pqDto.getNegotiationHistoryId());
        pq.setRemoteOfferId(pqDto.getRemoteOfferId());

        pq.setIsFinal(Boolean.TRUE.equals(pqDto.getIsFinal()));


        pq.setVendorId(pqDto.getVendorId());
        pq.setVendorName(pqDto.getVendorName());
        pq.setVendorEmail(pqDto.getVendorEmail());
        pq.setVendorPhoneNo(pqDto.getVendorPhoneNo());
        pq.setVendorType(pqDto.getVendorType());
        if(!pqDto.getTermsAndConditions().isEmpty()) {
            pq.setTermsAndConditions(pqDto.getTermsAndConditions().stream().map(tnc -> {
                PqTermsAndCondition pqTermsAndCondition = new PqTermsAndCondition();
                pqTermsAndCondition.setPriceQuotation(pq);
                pqTermsAndCondition.setTermAndCondition(tnc);
                pqTermsAndCondition.setVendorId(pqDto.getVendorId());
                pqTermsAndCondition.setRfq(indent);
                return pqTermsAndCondition;
            }).toList());
        }
        if(priceQuotationStateStatus.equals(PriceQuotationStateStatus.SENT)){
            Optional<PqRepository.PriceQuotationInfo> pqOp = pqRepository.getPrevPqByVendorId(pq.getVendorId());
            pqOp.ifPresent(epq->pq.setScore(epq.getScore()));

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
            pqd.setTotalPrice(pqd.getUnitPrice().multiply(detail.getRfqQty()));
            pqd.setEstDeliveryDays(detail.getEstDeliveryDays());
            pqd.setDeliveryDetails(detail.getDeliveryDetails().stream().map(pqdrd->{
                PriceQuotationDeliveryDetail pqdd = new PriceQuotationDeliveryDetail();
                pqdd.setDeliveryCharge(pqdrd.getDeliveryChargeType());
                pqdd.setDeliveryChargeAmount(pqdrd.getDeliveryChargeAmount());
                Optional<Warehouse> warehouseOp = warehouseService.getWarehouse(pqdrd.getWarehouseId());
                if(warehouseOp.isEmpty()){
                    throw new AesException("Warehouse not found");
                }
                if(priceQuotationStateStatus.equals(PriceQuotationStateStatus.SENT)){
                    Optional<DeliveryDetailDto> deliveryDetailDtoOp = pqDto.getWarehouses().stream().filter(w->
                       w.getWarehouseId().equals(pqdrd.getWarehouseId())
                    ).findFirst();
                    deliveryDetailDtoOp.ifPresent(deliveryDetailDto->{
                        Optional<ItemDeliveryDetailDto> itemOp = deliveryDetailDto.getItems().stream()
                                .filter(itemDeliveryDetailDto ->
                                                itemDeliveryDetailDto.getItemName()
                                                        .equals(detail.getItemAttributeName())
                        ).findFirst();
                        itemOp.ifPresent(itm->
                            pqdd.setDeliveryOrderQty(itm.getDeliveryOrderQty())
                        );

                    });
                }else {
                    pqdd.setDeliveryOrderQty(pqdrd.getDeliveryOrderQty());
                }
                pqdd.setWarehouse(warehouseOp.get());
                pqdd.setPriceQuotationDetail(pqd);
                return pqdd;
            }).toList());

            return pqd;
        }).toList());

        PriceQuotation pqSaved =pqRepository.save(pq);

        this.savePriceQuotationSummary(pq,pqDto);

        if(priceQuotationStateStatus.equals(PriceQuotationStateStatus.SENT)){
            CounterPqDto offerRequestDto = preparePayloadForSentCounterOffer(pqDto);
            Optional<Long> remoteOfferIdOp = tenderService.sentCounterOffer(claimResolver, indent, offerRequestDto);
            pq.setRemoteOfferId(remoteOfferIdOp.orElse(null));
        }
        return pqSaved.getId();
    }

    @Transactional
    private void savePriceQuotationSummary(PriceQuotation priceQuotation, PriceQuotationReqDto pqDto){
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
        if(pqDto.getPriceQuotationSummary()!=null && pqDto.getPriceQuotationSummary().getAitAmount() !=null){
            if(pqDto.getPriceQuotationSummary().getVatAmount().contains(".")){
                pqs.setAitAmount(BigDecimal.valueOf(Double.parseDouble(pqDto.getPriceQuotationSummary().getAitAmount())));
            }else{
                pqs.setAitAmount(BigDecimal.valueOf(Long.parseLong(pqDto.getPriceQuotationSummary().getAitAmount())));
            }

        }
        if(pqDto.getPriceQuotationSummary()!=null && pqDto.getPriceQuotationSummary().getVatPercent() !=null){
            String vatPercent = pqDto.getPriceQuotationSummary().getVatPercent();
            pqs.setVatPercent(BigDecimal.valueOf(Double.parseDouble(vatPercent)));
        }
        if(pqDto.getPriceQuotationSummary()!=null && pqDto.getPriceQuotationSummary().getAitPercent() !=null){
            String aitPercent = pqDto.getPriceQuotationSummary().getAitPercent();
            pqs.setAitPercent(BigDecimal.valueOf(Double.parseDouble(aitPercent)));
        }
        pqs.setSubTotalPrice(pqDto.getPriceQuotationSummary().getSubTotalPrice());
        pqs.setTotalPrice(pqDto.getPriceQuotationSummary().getTotalPrice());
        pqs.setPriceQuotation(priceQuotation);
        pqSummaryRepository.save(pqs);
    }

    private CounterPqDto preparePayloadForSentCounterOffer(PriceQuotationReqDto pqDto){
        CounterPqDto offerRequestDto = new CounterPqDto();
        offerRequestDto.setCreditPaymentDays(pqDto.getPriceQuotationSummary().getCreditPaymentDuration());
        offerRequestDto.setCreditType(pqDto.getPaymentMethod());
        offerRequestDto.setVatIncluded(pqDto.getPriceQuotationSummary().getIsVatAdded());
        offerRequestDto.setAitIncluded(pqDto.getPriceQuotationSummary().getIsAitAdded());
        offerRequestDto.setVatPercent(BigDecimal.valueOf(Double.parseDouble(pqDto.getPriceQuotationSummary().getVatPercent())));
        offerRequestDto.setAitPercent(BigDecimal.valueOf(Double.parseDouble(pqDto.getPriceQuotationSummary().getAitPercent())));
        if(pqDto.getPriceQuotationSummary().getVatAmount().contains(".")){
            offerRequestDto.setVatAmount(BigDecimal.valueOf(Double.parseDouble(pqDto.getPriceQuotationSummary().getVatAmount())));
        }else{
            offerRequestDto.setVatAmount(BigDecimal.valueOf(Long.parseLong(pqDto.getPriceQuotationSummary().getVatAmount())));
        }

        if(pqDto.getPriceQuotationSummary().getAitAmount().contains(".")){
            offerRequestDto.setAitAmount(BigDecimal.valueOf(Double.parseDouble(pqDto.getPriceQuotationSummary().getAitAmount())));
        }else{
            offerRequestDto.setAitAmount(BigDecimal.valueOf(Long.parseLong(pqDto.getPriceQuotationSummary().getAitAmount())));
        }

        offerRequestDto.setIsFinal(pqDto.getIsFinal());
        offerRequestDto.setNegotiationHistoryId(pqDto.getNegotiationHistoryId());

        offerRequestDto.setMushakIncluded(Boolean.TRUE.equals(pqDto.getPriceQuotationSummary().getMushak()));

        offerRequestDto.setFinalOfferPrice(pqDto.getPriceQuotationSummary().getSubTotalPrice());
        offerRequestDto.setTotalDeliveryChargeAmount(pqDto.getPriceQuotationSummary().getDeliveryChargeAmount());

        offerRequestDto.setNote(pqDto.getPriceQuotationSummary().getNote());
        List<CounterItemDto> offerItems = new ArrayList<>();


        pqDto.getDetails().forEach(d->{
            CounterItemDto offerItemDto = new CounterItemDto();
            offerItemDto.setWarrantyDuration(d.getWarrantyDuration());
            offerItemDto.setWarrantyUnit(d.getWarrantyUnit());
            offerItemDto.setEstimatedDeliveryDays(Long.valueOf(d.getEstDeliveryDays()));
            offerItemDto.setItemQuantity(d.getRfqQty());
            offerItemDto.setBrandName(d.getBrandName());
            offerItemDto.setProductDescription(d.getItemAttributeName());
            offerItemDto.setSpecification("Must be a good condition");
            CounterPriceQuotation opq = new CounterPriceQuotation();
            opq.setTotalPrice(d.getUnitPrice().multiply(d.getRfqQty()));
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
            odd.setDeliveryChargeMode(pd.getDeliveryChargeMode());
            odd.setWarehouseId(pd.getWarehouseId());
            odd.setItems(pd.getItems().stream().map(pdid->{
                ItemDeliveryDetailDto oidd= new ItemDeliveryDetailDto();
                oidd.setDeliveryOrderQty(pdid.getDeliveryOrderQty());
                oidd.setItemName(pdid.getItemName());
                return oidd;
            }).toList());
            return odd;
        }).toList());
        return offerRequestDto;
    }

    @Override
    public List<PqRepository.PriceQuotationInfo> getPriceQuotationsByIndent(Long id) {
        return pqRepository.getPriceQuotationsByIndentId(id);
    }
    @Override
    public Optional<Map<String,Object>> getDetail(Long id) {
        Optional<PriceQuotation> pqOptional = pqRepository.findById(id);
        if(pqOptional.isEmpty()){
            throw new AesException("Sorry! Price quotation not found by this id");
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
        return Optional.of(result);
    }

    @Override
    public List<NegotiationHistory> getHistoriesByRfq(Long id, Long vendorId) {
        List<PriceQuotation> priceQuotations = pqRepository.findByRfqIdAndVendorId(id,vendorId);



        return priceQuotations.stream().map(priceQuotation->{
            String sb = "";
            if(priceQuotation.getIsFinal()!=null && priceQuotation.getIsFinal().equals(true)){
                sb = sb.concat("Final ");
            }
            if(priceQuotation.getPriceQuotationStatus().equals(PriceQuotationStatus.INIT)){
                sb = sb.concat("Initial Quotation Received From "+priceQuotation.getVendorName());
            }
            if(priceQuotation.getPriceQuotationStatus().equals(PriceQuotationStatus.COUNTER_TO_VENDOR)){
                sb = sb.concat("Counter Offer to "+priceQuotation.getVendorName());
            }
            if(priceQuotation.getPriceQuotationStatus().equals(PriceQuotationStatus.COUNTER_TO_COMPANY)){
                sb = sb.concat("Counter Offer Received from "+priceQuotation.getVendorName());
            }
            return new NegotiationHistory(priceQuotation.getId(), sb);
        }).toList();
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
        if(priceQuotation.getRemoteOfferId()!=null) {
            setRemoteOfferStatus(status, priceQuotation.getRemoteOfferId(), priceQuotation.getVendorId(), noteDto);
        }

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
        this.savePq(indentOp,pqDto,PriceQuotationStatus.COUNTER_TO_VENDOR,PriceQuotationStateStatus.SENT);
    }

    @Override
    @Transactional
    public void addManualPq(Jwt token, PriceQuotationReqDto pqDto) {
        claimResolver.setToken(token);
        Optional<Indent> indentOp = indentService.getIndentFactory(pqDto);
        Long id = this.savePq(indentOp,pqDto,PriceQuotationStatus.INIT,PriceQuotationStateStatus.RECEIVED);
        lockPq(token,id, PriceQuotationStateStatus.LOCKED);
    }

    @Override
    @Transactional
    public void recommendPq(Jwt token, Long id) {
        claimResolver.setToken(token);

        setStatus(id,PriceQuotationStateStatus.AWARDED,pq->{
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
        this.savePq(indentOp,pqDto,PriceQuotationStatus.COUNTER_TO_COMPANY,PriceQuotationStateStatus.RECEIVED);
    }

    @Override
    public FileUploadResponse uploadDoc(Long rfqId, MultipartFile file) {
        if(Boolean.FALSE.equals(fileUploadService.checkMimeType(file.getContentType(),
                "application/pdf",
                "image/jpeg","image/png",
                "application/vnd.oasis.opendocument.text",
                "application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/csv"))){
            throw new RuntimeException("Sorry! not file not a valid type (pdf,odt,doc,docx,xls,xlsx,csv)");
        }
        Path path = Path.of(uploadDir+DIR_SEPARATOR+rfqId+"/pq/", file.getOriginalFilename());
        return fileUploadService.uploadFile(path, file);
    }
}
