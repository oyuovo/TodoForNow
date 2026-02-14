package justtodobe.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO {
    private Boolean success;
    private String errorCode;
    private Object data;

    public static ResultDTO ok(){
        return new ResultDTO(true, "200", null);
    }
    public static ResultDTO ok(Object data){
        return new ResultDTO(true,"200", data);
    }
    public static ResultDTO fail(String errorMsg){
        return new ResultDTO(false,"400" , errorMsg);
    }
}

