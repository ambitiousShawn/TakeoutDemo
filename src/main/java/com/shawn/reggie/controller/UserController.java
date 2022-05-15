package com.shawn.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shawn.reggie.common.R;
import com.shawn.reggie.entity.User;
import com.shawn.reggie.service.UserService;
import com.shawn.reggie.utils.ValidateCodeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    @PostMapping("/sendMsg")
    public R <String> sendMsg(@RequestBody User user, HttpSession session){
        //获取用户手机号
        String phone = user.getPhone();
        //生成随机验证码
        if (StringUtils.isNotEmpty(phone)){
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            System.out.println("code = "+code);

            //调用阿里云短信服务发送短信
            //SMSUtils.sendMessage("肖恩外卖","",phone,code);

            /*存到session
            session.setAttribute(phone,code);*/

            //存到Redis缓存中，并设置过期时间为5min
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return R.success("手机验证码短信发送成功");
        }

        return R.error("短信发送失败");


    }

    /**
     * 移动端登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login (@RequestBody Map map,HttpSession session){
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        /*从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);*/

        //从Redis缓存中获取验证码
        Object codeInRedis = redisTemplate.opsForValue().get(phone);

        //比对
        if (codeInRedis != null && codeInRedis.equals(code)){
            //如果能比对成功，说明登陆成功
            //判断当前用户是否为新用户，如果是新用户就自动完成注册
            LambdaQueryWrapper <User>queryWrapper = new LambdaQueryWrapper <>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            if (user == null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);//数据库默认值为1，可以去掉
                userService.save(user);
            }
            session.setAttribute("user",user.getId());

            //用户登录成功，可从缓存中删除验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }
        return R.error("登录失败，请重试");
    }


}
