package com.github.dimitryivaniuta.gateway.aml.util;

import java.text.Normalizer;
import java.util.Map;

/**
 * Normalization utilities for matching.
 */
public final class Normalize {

  private static final Map<Character, String> CYR = Map.ofEntries(
      Map.entry('А',"A"),Map.entry('а',"a"),
      Map.entry('Б',"B"),Map.entry('б',"b"),
      Map.entry('В',"V"),Map.entry('в',"v"),
      Map.entry('Г',"G"),Map.entry('г',"g"),
      Map.entry('Д',"D"),Map.entry('д',"d"),
      Map.entry('Е',"E"),Map.entry('е',"e"),
      Map.entry('Ё',"E"),Map.entry('ё',"e"),
      Map.entry('Ж',"Zh"),Map.entry('ж',"zh"),
      Map.entry('З',"Z"),Map.entry('з',"z"),
      Map.entry('И',"I"),Map.entry('и',"i"),
      Map.entry('Й',"I"),Map.entry('й',"i"),
      Map.entry('К',"K"),Map.entry('к',"k"),
      Map.entry('Л',"L"),Map.entry('л',"l"),
      Map.entry('М',"M"),Map.entry('м',"m"),
      Map.entry('Н',"N"),Map.entry('н',"n"),
      Map.entry('О',"O"),Map.entry('о',"o"),
      Map.entry('П',"P"),Map.entry('п',"p"),
      Map.entry('Р',"R"),Map.entry('р',"r"),
      Map.entry('С',"S"),Map.entry('с',"s"),
      Map.entry('Т',"T"),Map.entry('т',"t"),
      Map.entry('У',"U"),Map.entry('у',"u"),
      Map.entry('Ф',"F"),Map.entry('ф',"f"),
      Map.entry('Х',"Kh"),Map.entry('х',"kh"),
      Map.entry('Ц',"Ts"),Map.entry('ц',"ts"),
      Map.entry('Ч',"Ch"),Map.entry('ч',"ch"),
      Map.entry('Ш',"Sh"),Map.entry('ш',"sh"),
      Map.entry('Щ',"Shch"),Map.entry('щ',"shch"),
      Map.entry('Ы',"Y"),Map.entry('ы',"y"),
      Map.entry('Э',"E"),Map.entry('э',"e"),
      Map.entry('Ю',"Yu"),Map.entry('ю',"yu"),
      Map.entry('Я',"Ya"),Map.entry('я',"ya"),
      Map.entry('Ь',""),Map.entry('ь',""),
      Map.entry('Ъ',""),Map.entry('ъ',"")
  );

  private Normalize() {}

  public static String transliterateCyrillic(String s) {
    if (s == null) return null;
    StringBuilder b = new StringBuilder(s.length() * 2);
    for (char c : s.toCharArray()) {
      String rep = CYR.get(c);
      b.append(rep != null ? rep : c);
    }
    return b.toString();
  }

  public static String norm(String s) {
    if (s == null) return null;
    String t = transliterateCyrillic(s);
    t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    t = t.toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    return t;
  }
}
