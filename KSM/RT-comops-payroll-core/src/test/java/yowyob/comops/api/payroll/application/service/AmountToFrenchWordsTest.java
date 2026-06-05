package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AmountToFrenchWordsTest {

    @Test
    void handlesFrenchSpecificTens() {
        assertThat(AmountToFrenchWords.words(0)).isEqualTo("zéro");
        assertThat(AmountToFrenchWords.words(17)).isEqualTo("dix-sept");
        assertThat(AmountToFrenchWords.words(21)).isEqualTo("vingt et un");
        assertThat(AmountToFrenchWords.words(22)).isEqualTo("vingt-deux");
        assertThat(AmountToFrenchWords.words(71)).isEqualTo("soixante et onze");
        assertThat(AmountToFrenchWords.words(72)).isEqualTo("soixante-douze");
        assertThat(AmountToFrenchWords.words(80)).isEqualTo("quatre-vingts");
        assertThat(AmountToFrenchWords.words(81)).isEqualTo("quatre-vingt-un");
        assertThat(AmountToFrenchWords.words(91)).isEqualTo("quatre-vingt-onze");
        assertThat(AmountToFrenchWords.words(99)).isEqualTo("quatre-vingt-dix-neuf");
    }

    @Test
    void handlesHundredsPluralisation() {
        assertThat(AmountToFrenchWords.words(100)).isEqualTo("cent");
        assertThat(AmountToFrenchWords.words(200)).isEqualTo("deux cents");
        assertThat(AmountToFrenchWords.words(201)).isEqualTo("deux cent un");
        assertThat(AmountToFrenchWords.words(332)).isEqualTo("trois cent trente-deux");
    }

    @Test
    void handlesThousandsAndMillions() {
        assertThat(AmountToFrenchWords.words(1000)).isEqualTo("mille");
        assertThat(AmountToFrenchWords.words(2000)).isEqualTo("deux mille");
        // typical payroll net
        assertThat(AmountToFrenchWords.words(332066))
                .isEqualTo("trois cent trente-deux mille soixante-six");
        // typical final settlement
        assertThat(AmountToFrenchWords.words(1326881))
                .isEqualTo("un million trois cent vingt-six mille huit cent quatre-vingt-un");
    }

    @Test
    void rendersMoneyWithCurrencyAndCapital() {
        assertThat(AmountToFrenchWords.moneyInWords(new BigDecimal("332066"), "francs CFA"))
                .isEqualTo("Trois cent trente-deux mille soixante-six francs CFA");
    }
}
