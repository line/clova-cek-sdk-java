/*
 * Copyright 2018 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.clova.extension.boot.message.speech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A {@link OutputSpeech} generator
 *
 * @see MessageCode
 */
@RequiredArgsConstructor
@Slf4j
public class OutputSpeechGenerator {

    private final MessageSource messageSource;

    /**
     * Alternative to {@link #generateFrom(MessageCode, Locale, Object...)}
     * using a code only.
     * <p>
     * The locale for {@link SpeechInfoObject} is chosen from
     * {@link LocaleContextHolder}.
     *
     * @param messageCode the code to look up
     * @param args        an array arguments that will filled for params with in the message.
     *                    See detail at {@link MessageSource#getMessage(String, Object[], Locale)}
     * @return the generated OutputSpeech
     */
    public OutputSpeech generateFrom(MessageCode messageCode, Object... args) {
        return generateFrom(messageCode, LocaleContextHolder.getLocale(), args);
    }

    /**
     * Generate an {@link OutputSpeech} from a message via {@link MessageSource}.
     *
     * @param messageCode The code to lookup via MessageSource and concrete class of {@link MessageCode}.
     *                    As an example, will return 'foo.bar' when the method 'getCode' called.
     *                    OutputSpeechGenerator has a specific key called "brief" as part of its lookup,
     *                    so users of this generator should prepare code containing a "brief" keyword.
     *                    e.g. prepare "foo.bar.brief" at message source and user "foo.bar" at returning value of
     *                    implemented class on messageCode parameter.
     * @param locale      The locale for {@link SpeechInfoObject}
     * @param args        An array of arguments that will fill for params with in the message.
     *                    See detail at {@link MessageSource#getMessage(String, Object[], Locale)}
     * @return the generated OutputSpeech
     */
    public OutputSpeech generateFrom(MessageCode messageCode, Locale locale, Object... args) {
        String message = messageSource.getMessage(messageCode.getCode() + ".brief", args, locale);
        return OutputSpeech.text(message, locale);
    }

    /**
     * Alternative to {@link #generateFrom(Throwable, Locale, Object...)}
     * with any throwable.
     *
     * @param t    the exception
     * @param args additional information
     * @return the generated OutputSpeech, may be null if neither {@code .brief} nor {@code .verbose} exists
     */
    public OutputSpeech generateFrom(Throwable t, Object... args) {
        return generateFrom(t, LocaleContextHolder.getLocale(), args);
    }

    /**
     * Generate an {@link OutputSpeech} using a throwable object and locale.
     * <p>
     * Speech text is set via {@link MessageSource}. Users of this generator
     * should prepare code based on a throwable class name, such as 'java.lang.Throwable.brief'.
     * <p>
     * If you want more message for development, testing, etc., you can set additional text with the '.verbose' code,
     * e.g. 'java.lang.Throwable.verbose'.
     * This generator only generates a brief message. However, if verbose a message
     * exists, generate both an OutputSpeech brief and verbose message.
     * <p>
     * Normally, generated OutputSpeech is the "SimpleSpeech" type. If a message definition includes a comma,
     * generateFrom method generates a "SpeechList" type OutputSpeech.
     *
     * @param t      the exception. If code is not defined, generator will search ancestor code.
     * @param locale the locale for {@link SpeechInfoObject}
     * @param args   additional information
     * @return the generated OutputSpeech, may be null if neither {@code .brief} nor {@code .verbose} exists
     */
    public OutputSpeech generateFrom(Throwable t, Locale locale, Object... args) {
        MessageCodes messageCodes = getMessageCodes(t);
        List<String> briefMessages = getMessages(messageCodes.getBrief(), args, locale);
        List<String> verboseMessages = getMessages(messageCodes.getVerbose(), args, locale);

        if (briefMessages.isEmpty() && verboseMessages.isEmpty()) {
            log.warn("Not found any messages for {}(locale:{}). Check your message resources.",
                     t.getClass(), locale);
            return null;
        }

        OutputSpeech.OutputSpeechBuilder builder = OutputSpeech.builder();

        // Simple message pattern.
        if (verboseMessages.isEmpty()) {
            // SimpleSpeech or SpeechList
            briefMessages.stream()
                         .map(message -> SpeechInfoObject.text(message, locale))
                         .forEach(builder::value);
            return builder.build();
        }

        // Brief & Verbose pattern

        // Brief message should have only one message.
        if (briefMessages.size() != 1) {
            // Don't throw any exception. But only first message is used.
            log.error("Brief message should have only one message. [throwable:{}, locale:{}, messages:{}]",
                      t.getClass().getName(), locale, briefMessages);
        }

        if (!briefMessages.isEmpty()) {
            builder.brief(SpeechInfoObject.text(briefMessages.get(0), locale));
        }

        Verbose.VerboseBuilder verboseBuilder = Verbose.builder();
        verboseMessages.stream()
                       .map(message -> SpeechInfoObject.text(message, locale))
                       .forEach(verboseBuilder::value);

        return builder.verbose(verboseBuilder.build()).build();
    }

    List<String> getMessages(String[] codes, Object[] args, Locale locale) {
        String messages = messageSource.getMessage(
                new DefaultMessageSourceResolvable(codes, args, ""), locale);
        if (messages.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(messages.split("\\s*,\\s*"));
    }

    MessageCodes getMessageCodes(Throwable t) {
        MessageCodes messageCodes = new MessageCodes();
        messageCodes.add(t.getClass().getName());

        Class<?> superClass = t.getClass().getSuperclass();
        while (superClass != Object.class) {
            messageCodes.add(superClass.getName());
            superClass = superClass.getSuperclass();
        }
        return messageCodes;
    }

    private static class MessageCodes {

        List<String> brief = new ArrayList<>();
        List<String> verbose = new ArrayList<>();

        void add(String code) {
            brief.add(code + "." + MessageType.BRIEF.name().toLowerCase());
            verbose.add(code + "." + MessageType.VERBOSE.name().toLowerCase());
        }

        String[] getBrief() {
            return brief.toArray(new String[] {});
        }

        String[] getVerbose() {
            return verbose.toArray(new String[] {});
        }
    }

    private enum MessageType {
        BRIEF,
        VERBOSE;
    }

}
