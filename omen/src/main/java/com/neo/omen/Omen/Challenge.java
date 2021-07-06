package com.neo.omen.Omen;

import com.neo.omen.Omen.Body.AllChallengeListBody;
import com.neo.omen.Omen.Body.ChallengePostBody;
import com.neo.omen.Omen.Body.CurrentChallengeListBody;
import com.neo.omen.Omen.Body.JoinChallengeBody;
import com.neo.omen.Utils.HTTP.HttpUtil;
import com.neo.omen.Utils.HTTP.HttpUtilEntity;
import com.neo.omen.Utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author neo
 * @Date 2021/7/27 15:30
 * @Version 1.0
 **/
@Slf4j
public class Challenge {

    public static final String API = "https://rpc-prod.versussystems.com/rpc";
    public static final Map<String, String> HEADERS = new HashMap<String, String>() {{
        put("Content-Type", "application/json; charset=utf-8");
        put("User-Agent", "");
    }};
    private final String applicationId;
    private final String sessionToken;

    public Challenge(String applicationId, String sessionToken) {
        this.applicationId = applicationId;
        this.sessionToken = sessionToken;
    }

    /**
     * 获取所有挑战，对应[Available Rewards]
     *
     * @return String[] {challengeStructureId, campaignId, relevantEvent}
     */
    public List<String[]> getAllList() {
        Map<String, Object> body = new AllChallengeListBody(applicationId, sessionToken).genBody();
        List<String[]> allList = new ArrayList<>();
        HttpUtilEntity ret = request(body);
        if (ret == null) return allList;

        Map listMap = JsonUtil.string2Obj(ret.getBody(), Map.class);
        List<Map<String, Object>> collection = (List<Map<String, Object>>) (((Map) listMap.get("result")).get("collection"));
        //存储临时 campaignId,去重
        HashSet<String> currentCampaignIdtSet = new HashSet();
        collection.forEach(item -> {
            String challengeStructureId = (String) (item.get("challengeStructureId"));
            List<String> relevantEvents = (List<String>) item.get("relevantEvents");
            Map<String, Object> prize = (Map<String, Object>) (item.get("prize"));
            //log.info(String.valueOf(prize));
            String category = (String) prize.get("category");
            String campaignId = (String) prize.get("campaignId");
            //log.info("任务id:"+campaignId);
            synchronized (campaignId) {
                if ("sweepstake".equals(category)) {
                    if (!currentCampaignIdtSet.contains(campaignId)) {
                        allList.add(new String[]{
                                challengeStructureId,
                                campaignId,
                                relevantEvents.get(0)
                        });
                        currentCampaignIdtSet.add(campaignId);
                        log.info("挑战Id:" + challengeStructureId + ",任务Id:" + campaignId + ",游玩任务:" + relevantEvents.get(0));
                    }


                }
            }

        });

        return allList;
    }

    // 参加挑战
    public boolean join(String challengeStructureId, String campaignId) {
        Map<String, Object> body = new JoinChallengeBody(applicationId, sessionToken).genBody(campaignId, challengeStructureId);
        //log.info("任务id"+campaignId);
        HttpUtilEntity ret = request(body);
        if (ret == null) return false;
        return true;
    }

    // 获取进行中的任务
    public List<Map<String, Object>> currentList() {
        Map<String, Object> body = new CurrentChallengeListBody(applicationId, sessionToken).genBody();
        List<Map<String, Object>> currentList = new ArrayList<>();
        HttpUtilEntity ret = request(body);
        if (ret == null) return null;

        Map listMap = JsonUtil.string2Obj(ret.getBody(), Map.class);
        List<Map<String, Object>> collection = (List<Map<String, Object>>) (((Map) listMap.get("result")).get("collection"));

        collection.forEach(item -> {
            List<String> relevantEvents = (List<String>) item.get("relevantEvents");
            Map<String, Object> prize = (Map<String, Object>) (item.get("prize"));
            int progressPercentage = (int) item.get("progressPercentage");
            String category = (String) prize.get("category");
            if ("sweepstake".equals(category)) {
                currentList.add(new HashMap<String, Object>() {{
                    put("eventName", relevantEvents.get(0));
                    put("progress", progressPercentage);
                }});
            }
        });

        return currentList;
    }

    // 执行挑战
    public Map<String, Object> doIt(String eventName, int playTime) {
        Map<String, Object> body = new ChallengePostBody(applicationId, sessionToken).genBody(eventName, playTime);

        // 发送请求
        log.info("开始发送请求");
        HttpUtilEntity ret = request(body);
        if (ret == null) return null;
        log.info("请求完毕");

        // 解析结果
        Map<String, Object> retMap = JsonUtil.string2Obj(ret.getBody(), Map.class);
        List<Map<String, Object>> result = (List<Map<String, Object>>) retMap.get("result");

        Map<String, Object> item = result.get(0);
        List<String> relevantEvents = (List<String>) item.get("relevantEvents");
        int percentage = (int) item.get("progressPercentage");

        log.info("事件：{} -- 已完成 {}%", relevantEvents, percentage);
        return new HashMap<String, Object>() {{
            put("progress", percentage);
        }};
    }

    private HttpUtilEntity request(Map<String, Object> body) {
        HttpUtilEntity ret = null;
        try {
            ret = HttpUtil.doStreamPost(
                    API,
                    JsonUtil.obj2String(body).getBytes(StandardCharsets.UTF_8),
                    HEADERS
            );
            int code = ret.getStatusCode();
            if (code == 400) {
                Map result = JsonUtil.string2Obj(ret.getBody(), Map.class);
                Map<String, Object> error = (Map<String, Object>) result.get("error");
                log.info("SESSION过期，请更新 - 响应码：{}, {}", ret.getStatusCode(), error.get("message"));
                System.exit(-1);
                return null;
            } else if (code != 200) {
                log.info("响应异常-响应码：{}, {}", ret.getStatusCode(), ret);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
