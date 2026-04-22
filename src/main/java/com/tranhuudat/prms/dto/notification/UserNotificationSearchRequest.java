package com.tranhuudat.prms.dto.notification;

import com.tranhuudat.prms.dto.SearchRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

/**
 * read: null = tất cả, true = đã đọc, false = chưa đọc
 */
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserNotificationSearchRequest extends SearchRequest {
    Boolean read;
}
