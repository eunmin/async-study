# 비동기 프로그래밍과 Callback Hell

## 비동기 프로그래밍

- 범위를 좁히자.
- epoll(리눅스), kqueue(맥), iocp(윈도우)를 사용한 이벤트 방식 프로그래밍
- 동시성은 다루지 말자.
- 연속된 io 처리만 다뤄보자.
  ```javascript
  socket = connect()  // io wait
  send(socket)        // io wait
  data = recv(socket) // io wait
  ```
- 비동기 프로그램은 빨라지나? 성능에 대한 이야기

## 네트워크 IO 자바 예제

- https://github.com/eunmin/async-study/blob/master/src/main/java/ThreadExample.java

## 자바 비동기 프로그래밍 예제

- java.nio.channels.AsynchronousSocketChannel 를 사용한 비동기 프로그래밍 예제
  - https://github.com/eunmin/async-study/blob/master/src/main/java/NioExample.java

## Callback Hell

- 콜백 연쇄
- 체이닝을 이용한 해결
  - rx, complatablefuture, flux/mono ...
  - map 형태의 체인
  - flatMap 형태의 체인 -> 또 다른 callback hell?
- 문법적 지원으로 해결
  - async/await
  - coroutine
  - csp
- akka?
- kotlin flow?

## 함수형 프로그래밍
- IO 모나드의 flatMap 연쇄와 모나드 문법
