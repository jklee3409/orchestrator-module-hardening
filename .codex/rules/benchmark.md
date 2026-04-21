# Benchmark 규칙

## 필수 규칙

- JMeter benchmark asset은 반드시 `jmeter/` 하위에 둬야 한다.
- benchmark seed data는 명시적 SQL 또는 fixture 파일로 생성해야 한다.
- benchmark CSV user는 JMeter plan이 사용하는 authentication mode와 일치해야 한다.
- Redis benchmark와 DB-lock benchmark 비교는 동일한 bidder count, bid amount, feed setup, runtime profile을 사용해야 한다.
- benchmark 실행은 profile, JVM/app properties, JMeter properties, seed SQL version, dataset size를 기록해야 한다.
- benchmark 전용 security bypass는 명시적이어야 하며 기본값은 disabled여야 한다.

## 금지 규칙

- 서로 다른 seed data의 benchmark 결과를 같은 조건으로 비교하면 안 된다.
- reproducibility가 중요한 실행에서 `latest` tag, 암묵적 현재값, 기록되지 않은 runtime default를 사용하면 안 된다.
- benchmark 편의 설정을 production profile로 이동하면 안 된다.
- 명시적 요청 없이 생성된 benchmark report를 source commit에 섞으면 안 된다.
- benchmark summary에서 failed sampler rate 또는 error response를 숨기면 안 된다.

## 검증

- `jmeter/bid-benchmark.jmx`, bidder CSV, seed SQL의 user count, bid amount, test key, endpoint 가정이 서로 맞는지 확인해야 한다.
- benchmark에 사용하는 local properties가 production security posture를 바꾸지 않는지 확인해야 한다.
