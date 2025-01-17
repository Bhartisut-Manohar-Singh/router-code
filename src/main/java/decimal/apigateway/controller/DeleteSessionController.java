package decimal.apigateway.controller;

import decimal.apigateway.domain.Session;
import decimal.apigateway.model.RouterResponse;
import decimal.apigateway.repository.CacheClearRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DeleteSessionController {

    @Autowired
    CacheClearRepo cacheClearRepo;

    @PostMapping("deleteAllSessions")
    public Object deleteAllSessions() {
        try {
            cacheClearRepo.deleteAll();
            return new RouterResponse("SUCCESS","Sessions Deleted Successfullly",null);
        }catch (Exception e){
            e.printStackTrace();
            return new RouterResponse("FAILURE", "Failure While Deleting Sessions", null);
        }
    }


    @PostMapping("deleteSessionByOrgApp")
    public Object deleteSessionByOrgApp(@RequestHeader String orgid,@RequestHeader String appid,@RequestHeader String clientsecret) {
        try {
            List<Session> byOrgIdAndAppId = cacheClearRepo.findByOrgIdAndAppId(orgid, appid);
            cacheClearRepo.deleteAll(byOrgIdAndAppId);
            return new RouterResponse("SUCCESS","Sessions Deleted Successfullly",null);
        }catch (Exception e) {
            e.printStackTrace();
            return new RouterResponse("FAILURE", "Failure While Deleting Sessions", null);
        }
    }

    @PostMapping("deleteByCount")
    public Object deleteByCount(@RequestHeader String orgid,@RequestHeader String appid,@RequestHeader int count,@RequestHeader String date,@RequestHeader String clientSecret) {
        try {
            for (int i = 0; i <= count; i++) {
                List<Session> byOrgIdAndAppId = cacheClearRepo.findTop1000ByOrgIdAndAppIdOrderByLastLoginAsc(orgid, appid);
                cacheClearRepo.deleteAll(byOrgIdAndAppId);
            }
                return new RouterResponse("SUCCESS", "Sessions Deleted Successfullly", null);
            }catch(Exception e){
                e.printStackTrace();
                return new RouterResponse("FAILURE", "Failure While Deleting Sessions", null);
            }

    }
}
