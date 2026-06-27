package com.test.testApi.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;

// 預約結果：場次有名額則直接預約成功，已滿則自動轉為候補
@Data
@AllArgsConstructor
public class BookingResultRes {
    private boolean waitlisted;
    private String message;
    private ReservationRes reservation;
    private WaitlistRes waitlist;

    public static BookingResultRes reserved(ReservationRes reservation) {
        return new BookingResultRes(false, "預約成功", reservation, null);
    }

    public static BookingResultRes waitlisted(WaitlistRes waitlist) {
        return new BookingResultRes(true, "名額已滿，已加入候補名單", null, waitlist);
    }
}
