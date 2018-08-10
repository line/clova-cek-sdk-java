package com.linecorp.clova.extension.sample.echo_lambda;

import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.LaunchMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.SessionEndedMapping;
import com.linecorp.clova.extension.boot.handler.annnotation.SlotValue;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;


import static com.linecorp.clova.extension.boot.message.speech.OutputSpeech.text;

@CEKRequestHandler
@Slf4j
public class EchoHandler {

  @LaunchMapping
  CEKResponse handleLaunch() {
    return CEKResponse.builder()
        .outputSpeech(text("数字のオウム返しスキルを起動しました。"))
        .shouldEndSession(false)
        .build();
  }

  @IntentMapping("Clova.GuideIntent")
  CEKResponse handleGuideIntent() {
    return CEKResponse.builder()
        .outputSpeech(text("何か数字を言ってください。"))
        .shouldEndSession(false)
        .build();
  }

  @IntentMapping("Echo")
  CEKResponse handleRepeatIntent(@SlotValue Optional<Integer> number) {
    String outputSpeechText = number
        .map(num -> "数字は" + num + "ですね。")
        .orElse("聞き取れませんでした。");
    return CEKResponse.builder()
        .outputSpeech(text(outputSpeechText))
        .shouldEndSession(false)
        .build();
  }

  @IntentMapping("Clova.YesIntent")
  CEKResponse handleYesIntent() {
    return CEKResponse.builder()
        .outputSpeech(text("どうぞ"))
        .shouldEndSession(false)
        .build();
  }

  @IntentMapping("Clova.NoIntent")
  CEKResponse handleNoIntent() {
    return CEKResponse.builder()
        .outputSpeech(text("数字のオウム返しスキルを終了します。"))
        .shouldEndSession(true)
        .build();
  }

  @IntentMapping("Clova.CancelIntent")
  CEKResponse handleCancelIntent() {
    return CEKResponse.builder()
        .outputSpeech(text("数字のオウム返しスキルを終了します。"))
        .shouldEndSession(true)
        .build();
  }

  @SessionEndedMapping
  CEKResponse handleSessionEnded() {
    log.info("数字のオウム返しスキルを終了しました。");
    return CEKResponse.empty();
  }

}
