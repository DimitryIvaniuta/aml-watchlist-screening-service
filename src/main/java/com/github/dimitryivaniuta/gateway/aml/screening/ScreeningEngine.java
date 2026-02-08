package com.github.dimitryivaniuta.gateway.aml.screening;

import com.github.dimitryivaniuta.gateway.aml.domain.Transaction;
import com.github.dimitryivaniuta.gateway.aml.domain.WatchlistEntry;
import com.github.dimitryivaniuta.gateway.aml.util.Normalize;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.text.similarity.SimilarityScore;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.FuzzyScore;
import org.apache.commons.text.similarity.LongestCommonSubsequenceDistance;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.apache.commons.text.similarity.EditDistance;
import org.apache.commons.text.similarity.DoubleMetaphone;
import org.springframework.stereotype.Component;

/**
 * Strong matching engine:
 * - normalization + transliteration
 * - phonetics (DoubleMetaphone)
 * - multi-field scoring (name/dob/nationality/address)
 *
 * <p>Returns explainable reasons with contributions.
 */
@Component
public class ScreeningEngine {

  private final JaroWinklerSimilarity jw = new JaroWinklerSimilarity();
  private final DoubleMetaphone dm = new DoubleMetaphone();

  public MatchResult score(Transaction tx, WatchlistEntry e) {
    List<MatchReason> reasons = new ArrayList<>();

    String txName = Normalize.norm(tx.getCustomerFullName());
    String wlName = e.getFullNameNorm();

    double nameSim = safe(jw.apply(txName, wlName));
    double nameWeight = 0.55;
    double score = nameSim * nameWeight;
    reasons.add(new MatchReason("NAME_SIM", "Name similarity", nameSim * nameWeight));

    // Phonetics: compare metaphone codes of last tokens
    double phon = phoneticScore(txName, wlName);
    double phonWeight = 0.15;
    score += phon * phonWeight;
    reasons.add(new MatchReason("PHONETIC", "Phonetic similarity", phon * phonWeight));

    // DOB exact match (if present)
    double dob = dobScore(tx.getCustomerDob(), e.getDob());
    double dobWeight = 0.15;
    score += dob * dobWeight;
    if (tx.getCustomerDob() != null && e.getDob() != null) {
      reasons.add(new MatchReason("DOB", "Date of birth match", dob * dobWeight));
    }

    // Nationality / country
    double nat = countryScore(tx.getCustomerNationality(), e.getNationality(), tx.getCustomerCountry(), e.getCountry());
    double natWeight = 0.05;
    score += nat * natWeight;
    reasons.add(new MatchReason("COUNTRY", "Nationality/Country match", nat * natWeight));

    // Address similarity (best-effort)
    double addr = addressScore(tx, e);
    double addrWeight = 0.10;
    score += addr * addrWeight;
    reasons.add(new MatchReason("ADDRESS", "Address similarity", addr * addrWeight));

    return new MatchResult(e, clamp(score), reasons);
  }

  private double phoneticScore(String a, String b) {
    String la = lastToken(a);
    String lb = lastToken(b);
    if (la == null || lb == null) return 0.0;
    String ca = dm.doubleMetaphone(la);
    String cb = dm.doubleMetaphone(lb);
    if (ca == null || cb == null || ca.isBlank() || cb.isBlank()) return 0.0;
    return ca.equals(cb) ? 1.0 : 0.0;
  }

  private double dobScore(LocalDate a, LocalDate b) {
    if (a == null || b == null) return 0.0;
    return a.equals(b) ? 1.0 : 0.0;
  }

  private double countryScore(String natA, String natB, String cA, String cB) {
    double s = 0.0;
    if (natA != null && natB != null && natA.equalsIgnoreCase(natB)) s += 0.5;
    if (cA != null && cB != null && cA.equalsIgnoreCase(cB)) s += 0.5;
    return s;
  }

  private double addressScore(Transaction tx, WatchlistEntry e) {
    if (tx.getAddressLine1() == null || e.getAddressLine1() == null) return 0.0;
    String a = Normalize.norm(tx.getAddressLine1() + " " + nz(tx.getCity()) + " " + nz(tx.getPostalCode()) + " " + nz(tx.getAddressCountry()));
    String b = Normalize.norm(e.getAddressLine1() + " " + nz(e.getCity()) + " " + nz(e.getPostalCode()) + " " + nz(e.getAddressCountry()));
    return safe(jw.apply(a, b));
  }

  private static String nz(String s) { return s == null ? "" : s; }

  private static String lastToken(String s) {
    if (s == null) return null;
    String[] p = s.trim().split("\\s+");
    if (p.length == 0) return null;
    return p[p.length - 1];
  }

  private static double safe(Double v) { return v == null ? 0.0 : v; }
  private static double clamp(double v) { return Math.max(0.0, Math.min(1.0, v)); }
}
