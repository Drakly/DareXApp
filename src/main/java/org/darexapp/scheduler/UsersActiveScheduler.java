package org.darexapp.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.darexapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsersActiveScheduler {


    private final UserService userService;

    @Autowired
    public UsersActiveScheduler(UserService userService) {
        this.userService = userService;
    }


    @Scheduled(fixedRate = 300000)
    public void checkActiveUsers() {
        int activeUsersCount = userService.getActiveUserCount();
        log.info("Active users: " + activeUsersCount);
    }
}
