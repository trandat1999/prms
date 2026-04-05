package com.tranhuudat.prms.util;

public class ConstUtil {
    //system_role
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";
    //system_information
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";
    public static final String ADMIN_EMAIL = "nuclearshop123@gmail.com";
    //system_properties
    public static final String PATH_FILE_IMAGE_PROPERTY = "file.path.image";

    public static final String HOST_URL_PROPERTY = "url.host";
    public static final String CLIENT_URL_PROPERTY = "client.url";
    public static final String MAIL_TIME_EXPIRED_PROPERTY = "application.mail.time.expired.active";
    //regex
    public static final String REGEX_PHONE_NUMBER = "((84|0|'+'84)[3|5|7|8|9])+([0-9]{8})";
    public static  int TIME_MINUTES_EXPIRED_VERIFICATION_TOKEN = 5;

    public static final String API = "api";
    public static final String SLASH = "/";

    public static final String DOT = ".";
    public static final String COMMA = ",";
    public static final String SPACE = " ";
    public static final String UNDERLINE = "_";
    public static final String SEMICOLON = ";";
    public static final String EQUAL = "=";
    public static final String QUESTION_MARK = "?";
    public static final String AT = "@";
    public static final String PLUS = "+";
    public static final String COLON = ":";
    public static final String DASH = "-";
    public static final String XLSX = "xlsx";
    public static final String XLS = "xls";
    public static final String DOT_XLSX = ".xlsx";
    public static final String DOT_XLS = ".xls";


    public static final long TIMEOUT_TOKEN = 3000L;
    public static final long TIMEOUT_REFRESH_TOKEN = 18000L;
    public static String FILE_PATH_IMAGE = "";
    public static String HOST_URL = "http://localhost:8080";
    public static String CLIENT_URL = "http://localhost:4200";
    public static String URL_VERIFICATION_TOKEN = "auth/accountVerification";
    public static String CLIENT_URL_VERIFICATION_ACCOUNT = "verification-account";
    public static String CLIENT_URL_FORGOT_PASSWORD = "verification-account";
    public static final String URL_PUBLIC_FILE_DOWNLOAD = "/api/v1/publish/files/";
    public static final String MINE_TYPE_DOC_OR_DOT = "application/msword";
    public static final String MINE_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String MINE_TYPE_XLS = "application/vnd.ms-excel";
    public static final String MINE_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final int MIN_LENGTH_PASSWORD_REQUIRED = 6;
    public static final String PATTERN_USERNAME = "[a-zA-Z0-9]+";
    public static final String MAIL_TEMPLATE_ACTIVE_USER = "mail/register";
    public static final String PARAM_ACTIVE_ACCOUNT = "linkActive";
    public static final String PARAM_NEW_PASSWORD = "newPassword";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String LAST_MODIFIED_DATE = "lastModifiedDate";


    //kafka
    public static final String KAFKA_GROUP_ORDER = "Order";
    public static final String KAFKA_TOPIC_UPDATE_WAREHOUSE = "update_warehouse";

}
