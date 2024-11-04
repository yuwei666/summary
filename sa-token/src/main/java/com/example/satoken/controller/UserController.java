package com.example.satoken.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.example.satoken.domain.model.LoginBody;
import com.example.satoken.redis.RedisUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/")
public class UserController {

    // 测试登录，浏览器访问： http://localhost:8081/user/doLogin?username=zhang&password=123456
    @RequestMapping("doLogin")
    @SaIgnore
    public String doLogin(@Validated LoginBody loginBody) {
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
        if("zhang".equals(username) && "123456".equals(password)) {
            //StpUtil.login(10001);
            // 登录10001账号，并为生成的 Token 追加扩展参数name
            StpUtil.login(10001, SaLoginConfig.setExtra("name", "ZhangSan").setExtra("age", 18));

//            SaLoginModel model = new SaLoginModel();
//            StpUtil.login(10001, model.setExtra(USER_KEY, user_id));

            RedisUtils.setCacheObject("who", "i am");

            // 获取扩展参数
            String name = (String) StpUtil.getExtra("name");
            // 获取任意 Token 的扩展参数
            //String name = StpUtil.getExtra("tokenValue", "name");
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            return "登录成功\n" + tokenInfo;
        }
        return "登录失败";
    }

    // 查询登录状态，浏览器访问： http://localhost:8081/user/isLogin
    @RequestMapping("isLogin")
    public String isLogin() {

        SaStorage storage = SaHolder.getStorage();

        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    // 查询 Token 信息  ---- http://localhost:8081/acc/tokenInfo
    @RequestMapping("tokenInfo/{id}")
    //@SaIgnore
    public SaResult tokenInfo(@PathVariable("id") String id) {

        StpUtil.hasPermission("user.id");

        return SaResult.data(StpUtil.getTokenInfo());
    }

    // 测试注销  ---- http://localhost:8081/acc/logout
    @RequestMapping("logout")
    public SaResult logout() {
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        System.out.println(tokenInfo);
        StpUtil.logout();
        return SaResult.ok();
    }

}
