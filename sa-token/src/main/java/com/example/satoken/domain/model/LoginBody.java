package com.example.satoken.domain.model;

import com.example.validate.EditGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class LoginBody {

    @NotBlank(message = "{user.username.not.blank}")
    @Length(max = 10, min = 2)
    private String username;

    @NotBlank
    @Length(max = 10, min = 2)
    private String password;

    @NotBlank(groups = EditGroup.class)
    private String id;

    /**
     * 验证码，短信发的？
     */
    private String code;

    /**
     * 唯一标识，是注册时返回去的？
     */
    private String uuid;

    @Valid
    private List<UserVO> users;
}
