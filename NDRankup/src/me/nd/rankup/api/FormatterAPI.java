package me.nd.rankup.api;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.FileConfiguration;

import me.nd.rankup.Main;

public class FormatterAPI {
    private static final FileConfiguration CONFIG = Main.get().getConfig();
    private static final Pattern PATTERN = Pattern.compile("^(\\d+\\.?\\d*)(\\D+)$");
    private static final List<String> SUFFIXES = CONFIG.getStringList("Formatação").isEmpty() 
        ? Arrays.asList("", "K", "M", "B", "T") // Valores padrão caso a configuração esteja vazia
        : CONFIG.getStringList("Formatação");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    /**
     * Formata um número em uma string com sufixos (ex.: 1500000 -> "1.5M").
     * @param value O valor a ser formatado.
     * @return A string formatada com o sufixo apropriado.
     */
    public static String formatNumber(double value) {
        if (value < 0) {
            return "-" + formatNumber(-value); // Lida com números negativos
        }
        if (value < 1000) {
            return DECIMAL_FORMAT.format(value); // Valores menores que 1000 não têm sufixo
        }

        int index = 0;
        double scaledValue = value;
        while (scaledValue >= 1000 && index < SUFFIXES.size() - 1) {
            scaledValue /= 1000.0;
            index++;
        }

        return DECIMAL_FORMAT.format(scaledValue) + SUFFIXES.get(index);
    }

    /**
     * Converte uma string formatada (ex.: "1.5M") em um número double.
     * @param value A string a ser parseada.
     * @return O valor numérico correspondente.
     * @throws Exception Se o formato for inválido.
     */
    public static double parseString(String value) throws Exception {
        try {
            return Double.parseDouble(value); // Tenta parsear como número puro
        } catch (NumberFormatException e) {
            Matcher matcher = PATTERN.matcher(value.trim());
            if (!matcher.matches()) {
                throw new Exception("Invalid format: " + value);
            }

            double amount = Double.parseDouble(matcher.group(1));
            String suffix = matcher.group(2).toUpperCase();
            int index = SUFFIXES.indexOf(suffix);
            if (index == -1) {
                throw new Exception("Invalid suffix: " + suffix);
            }

            return amount * Math.pow(1000.0, index);
        }
    }

    /**
     * Altera a lista de sufixos usada para formatação.
     * @param suffixes A nova lista de sufixos.
     */
    public static void changeSuffixes(List<String> suffixes) {
        FormatterAPI.SUFFIXES.clear();
        FormatterAPI.SUFFIXES.addAll(suffixes.isEmpty() ? Arrays.asList("", "K", "M", "B", "T") : suffixes);
    }
}