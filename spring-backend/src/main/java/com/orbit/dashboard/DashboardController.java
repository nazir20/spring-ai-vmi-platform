package com.orbit.dashboard;

import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboard;

    @GetMapping
    public Response<DashboardSnapshot> snapshot() {
        return Response.<DashboardSnapshot>builder().statusCode(200).data(dashboard.snapshot()).build();
    }

    @PostMapping("/brief")
    public Response<String> brief(@RequestParam(required = false) Long siteId) {
        return Response.<String>builder().statusCode(200).data(dashboard.brief(siteId)).build();
    }
}
