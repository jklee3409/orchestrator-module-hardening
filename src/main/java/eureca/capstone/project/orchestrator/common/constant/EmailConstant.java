package eureca.capstone.project.orchestrator.common.constant;

public class EmailConstant {
    public static final String EMAIL_SUBJECT = "Datcha 회원가입 인증 메일 입니다.";
    public static String EMAIL_BODY = """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <title>Datcha 이메일 인증</title>
            </head>
            <body style="font-family: Arial, sans-serif; background-color: #f6f6f6; padding: 40px;">
                <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #5A3EEC;">Datcha 회원가입 인증</h2>
                    <p>안녕하세요, <strong>Datcha</strong>에 가입해주셔서 감사합니다.</p>
                    <p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요:</p>
                    <div style="text-align: left; margin: 30px 0;">
                        <a href="%s"
                           style="background-color: #5A3EEC; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-size: 16px;">
                            이메일 인증하기
                        </a>
                    </div>
                    <p style="font-size: 12px; color: #888;">이 메일은 발신 전용입니다.</p>
                </div>
            </body>
            </html>
            """;
}
