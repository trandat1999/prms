package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.notification.PushSubscriptionDeleteRequest;
import com.tranhuudat.prms.dto.notification.PushSubscriptionUpsertRequest;

public interface UserPushSubscriptionService {
    BaseResponse upsert(PushSubscriptionUpsertRequest request);

    BaseResponse delete(PushSubscriptionDeleteRequest request);

    BaseResponse mySubscriptions();
}

