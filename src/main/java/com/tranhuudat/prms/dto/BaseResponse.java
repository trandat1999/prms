package com.tranhuudat.prms.dto;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

/**
 * @author DatNuclear 04/05/2026 03:53 PM
 * @project prms
 * @package com.tranhuudat.prms.dto.response
 */
@Data
@Builder
public class BaseResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();
    private Object body;
    private String message;
    private String status;
    private Integer code;
}
