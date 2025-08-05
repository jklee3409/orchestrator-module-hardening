package eureca.capstone.project.orchestrator.common.constant;

public class EmailConstant {
    // 회원가입 이메일 인증
    public static final String EMAIL_SUBJECT = "Datcha 회원가입 인증 메일 입니다.";
    public static final String EMAIL_BODY = """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <title>Datcha 이메일 인증</title>
            </head>
            <body style="font-family: Arial, sans-serif; background-color: #f6f6f6; padding: 40px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #154D50;">Datcha 회원가입 인증</h2>
                    <p>안녕하세요, <strong>Datcha</strong>에 가입해주셔서 감사합니다.</p>
                    <p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요:</p>
                    <div style="text-align: left; margin: 30px 0;">
                        <a href="%s"
                           style="background-color: #154D50; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-size: 16px;">
                            이메일 인증하기
                        </a>
                    </div>
                    <p style="font-size: 12px; color: #888;">이 메일은 발신 전용입니다.</p>
                </div>
            </body>
            </html>
            """;

    // 비밀번호 재설정
    public static final String PASSWORD_RESET_SUBJECT = "Datcha 비밀번호 재설정 안내";
    public static final String PASSWORD_RESET_BODY = """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <title>Datcha 비밀번호 재설정</title>
            </head>
            <body style="font-family: Arial, sans-serif; background-color: #f6f6f6; padding: 40px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #154D50;">Datcha 비밀번호 재설정</h2>
                    <p>안녕하세요.</p>
                    <p>아래 버튼을 클릭하여 비밀번호 재설정을 완료해주세요. 이 링크는 15분간 유효합니다.</p>
                    <div style="text-align: left; margin: 30px 0;">
                        <a href="%s"
                           style="background-color: #154D50; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-size: 16px;">
                            비밀번호 재설정하기
                        </a>
                    </div>
                    <p style="font-size: 12px; color: #888;">본인이 요청하지 않으셨다면 이 메일을 무시해주세요.</p>
                </div>
            </body>
            </html>
            """;
}
