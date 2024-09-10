package com.agi.aesl.erpscm.quality_control.dto.request;

import com.agi.aesl.erpscm.comment.entity.CommentAttachment;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class NoteDto {

    @NotNull(message = "Sorry! note should not be null")
    @NotEmpty(message = "Sorry! note should not be blank")
    private String note;

    private List<CommentAttachment> attachments;

    public NoteDto(String note){
        attachments = new ArrayList<>();
        this.note = note;
    }
}
