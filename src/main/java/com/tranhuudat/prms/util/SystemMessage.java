package com.tranhuudat.prms.util;

public class SystemMessage {
    //message properties
    public static final String SUCCESS = "app.message.success";
    public static final String BAD_REQUEST = "app.message.badRequest";
    public static final String USER_IS_DISABLE = "app.message.userIsDisabled";
    public static final String USER_IS_EXPIRED = "app.message.userIsExpired";
    public static final String USER_CREDENTIALS_IS_EXPIRED = "app.message.userCredentialsIsExpired";
    public static final String USER_IS_LOCKED = "app.message.userIsLocked";
    public static final String USER_BAD_CREDENTIALS = "app.message.usernamePasswordIsInvalid";
    public static final String JWT_IS_EXPIRED = "app.message.jwtIsExpired";
    public static final String JWT_IS_INVALID = "app.message.jwtIsInvalid";
    public static final String ACCESS_DENIED = "app.message.accessDenied";
    public static final String UNAUTHORIZED = "app.message.unauthorized";
    public static final String FORBIDDEN = "app.message.forbidden";
    public static final String VALUE_EXIST = "app.message.valueExist";
    public static final String REGISTER_SUCCESS = "app.message.registerSuccess";
    public static final String SEND_MAIL_ERROR = "app.message.sendMailError";
    public static final String TOKEN_INVALID = "app.message.tokenInvalid";
    public static final String TOKEN_EXPIRED = "app.message.tokenExpired";
    public static final String VERIFY_SUCCESS = "app.message.verifySuccess";
    public static final String NOT_FOUND = "app.message.notFound";
    public static final String ACTIVATED = "app.message.activated";
    public static final String GENERATE_TOKEN_SUCCESS = "app.message.generateTokenSuccess";
    public static final String TWO_FIELD_NOT_MATCH = "app.message.twoFieldNotMatch";
    public static final String CONTENT_SUCCESS_FORGOT_PASS= "app.message.successForgotPass";
    public static final String ALREADY_SEND_FORGOT_PASS= "app.message.alreadySendForgotPass";
    public static final String LINK_EXPIRED= "app.message.linkAlreadyExpired";
    public static final String ACTIVE_NEW_PASS_SUCCESS= "app.message.activeNewPassSuccess";
    public static final String FILE_NAME_INVALID = "app.message.fileNameInvalid";
    public static final String WRITE_FILE_ERROR = "app.message.writeFileError";

    // validation
    public static final String VALIDATION_NOTNULL = "{app.validation.NotNull}";
    public static final String VALIDATION_NOTNULL_SV = "app.validation.NotNull";
    public static final String VALIDATION_NOT_BLANK = "{app.validation.NotBlank}";
    public static final String VALIDATION_EMAIL = "{app.validation.email}";
    public static final String VALIDATION_MIN_LENGTH = "{app.validation.MinLength}";
    public static final String VALIDATION_USERNAME_PATTERN = "{app.validation.usernamePattern}";
    public static final String VALIDATION_FIELD_MATCH = "{app.validation.fieldMatch}";

    // message application
    public static final String CONTENT_MAIL_REGISTER = "Thanks for registering your account. Link activity will be expired after 5 minutes";
    public static final String SUBJECT_MAIL_REGISTER = "Verify your email address";
    public static final String SUBJECT_MAIL_FORGOT = "Reset your password";
    public static final String CONTENT_MAIL_FORGOT = "Please active your new password. Link activity will be expired after 5 minutes";
}
