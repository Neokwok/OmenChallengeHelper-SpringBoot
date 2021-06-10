package com.neo.omen.service.impl;

import com.neo.omen.Omen.Challenge;
import com.neo.omen.Omen.Login;
import com.neo.omen.Utils.JsonUtil;
import com.neo.omen.domain.User;
import com.neo.omen.mapper.UserMapper;
import com.neo.omen.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    public static final Object object = new Object();

    @Autowired
    private UserMapper userMapper;

    @Override
    public void omen() {
        List<User> users = userMapper.getList();
        System.out.println("获取数据库中所有用户"+ users);
        for (User user: users) {
            System.out.println("-----------当前用户"+user+"-----------");

            String sessionToken;
            Login login = new Login(user.getEmail(),user.getPass());
            log.info("登录准备");
            login.webPrepare();
            String localhostUrl = login.webLogin();
            log.info("开始模拟Omen登录操作");
            String tokenInfo = login.clientLogin(localhostUrl);
            Map akMap = JsonUtil.string2Obj(tokenInfo, Map.class);

            // 设备处理，按需
            // Device device = new Device((String) akMap.get("access_token"));
            // device.sendInfo();
            // device.sendGetEmpty();
            // device.getDetail();
            // device.register();

            log.info("开始获取挑战SESSION");
            sessionToken = login.genSession((String) akMap.get("access_token"));

            // 生成数据
            String applicationId = "6589915c-6aa7-4f1b-9ef5-32fa2220c844";

            Challenge challenge = new Challenge(applicationId, sessionToken);

            log.info("获取可参与挑战列表");
            List<String[]> allList = challenge.getAllList();
            log.info("可加入的挑战数：{}", allList.size());
            allList.forEach(s->{
                log.info("加入挑战：{}", s);
                challenge.join(s[0], s[1]);
            });

            List<Map<String, Object>> eventList = challenge.currentList();
            log.info("待完成任务数：{}", eventList.size());
            //System.out.println("注意，时间设置细节：\n要小于等于当前时间减去上一次提交任务的时间");
            eventList.forEach(en-> {
                System.out.println("");
                log.info("当前执行任务：{} - {}%", en.get("eventName"), en.get("progress"));
                int time;
                if(((String) en.get("eventName")).startsWith("Launch")){
                    time = 1;
                }else {
                    time = 45;
                }
//                try{
//                    time = 45;
//                }catch (NumberFormatException e){
//                    System.out.println("输入字符非法，使用45作为默认值");
//                    // 默认值
//                    time = 45;
//                }
                Map<String, Object> result = challenge.doIt((String) en.get("eventName"), time);
                if((int)result.get("progress") == (int)en.get("progress")){
                    System.out.println("任务已领取，进度未发生变化，当前任务设置时间为"+time
                            +"分钟，请等待"+time+"分钟后重新执行程序！");
                }
            });

            run();

        }

        System.out.println("-------------------------end,所有账号HP任务执行完毕，45分钟重新执行程序即可完成全部任务-----------------------");



    }

    public static void run() {
        try {

            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("-----------当前操作需等待5秒-----------");
            System.out.println("开始时间--start@" + simpleDateFormat.format(new Date()));
            Thread.sleep(5000);
            System.out.println("结束时间----end@" + simpleDateFormat.format(new Date()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
