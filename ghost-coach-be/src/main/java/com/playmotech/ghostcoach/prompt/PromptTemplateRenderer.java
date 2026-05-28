package com.playmotech.ghostcoach.prompt;

import com.playmotech.ghostcoach.prompt.exception.PromptVariableMissingException;
import com.playmotech.ghostcoach.prompt.exception.PromptVariableUnknownException;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PromptTemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)}}");

    private PromptTemplateRenderer() {
    }

    public static String render(Prompt prompt, Map<String, Object> vars) {
        validateNoUnknownVars(prompt, vars);

        Matcher matcher = PLACEHOLDER.matcher(prompt.getTemplate());
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            Object value = vars.get(name);
            if (value == null) {
                throw new PromptVariableMissingException(prompt.getPromptKey(), name);
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private static void validateNoUnknownVars(Prompt prompt, Map<String, Object> vars) {
        Set<String> declared = Set.copyOf(prompt.getVariables());
        for (String key : vars.keySet()) {
            if (!declared.contains(key)) {
                throw new PromptVariableUnknownException(prompt.getPromptKey(), key);
            }
        }
    }
}
