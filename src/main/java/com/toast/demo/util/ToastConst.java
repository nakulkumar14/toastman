package com.toast.demo.util;

import java.util.List;

public interface ToastConst {

    List<String> COMMON_HEADERS = List.of(
        "Accept", "Accept-Encoding", "Authorization", "Cache-Control", "Content-Length",
        "Content-Type", "Cookie", "Date", "Host", "Origin", "Referer", "User-Agent",
        "X-Requested-With", "If-Modified-Since", "ETag", "Connection", "Accept-Language"
    );
}
