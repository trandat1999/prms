package com.tranhuudat.prms.service;

/**
 * @author DatNuclear 04/05/2026 04:20 PM
 * @project prms
 * @package com.tranhuudat.prms.service
 */
public interface MailService {
    void sendHtml(String to, String subject, String html);
}
