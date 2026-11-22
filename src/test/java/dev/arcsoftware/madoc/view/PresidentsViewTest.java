package dev.arcsoftware.madoc.view;

import dev.arcsoftware.madoc.model.entity.PresidentEntity;
import dev.arcsoftware.madoc.service.PresidentService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PresidentsViewTest {

    @Test
    void formatsOrdinalsInitialsAndTermsCorrectly() {
        PresidentEntity first = new PresidentEntity(1, "Joe Provenzano", 1973, 1975, null);
        PresidentEntity second = new PresidentEntity(2, "Brian Crutchlow", 1975, 1978, null);
        PresidentEntity third = new PresidentEntity(3, "Paul Collins Sr.", 1978, 1986, null);
        PresidentEntity eleventh = new PresidentEntity(11, "Brian Neil", 2021, null, "brian-neil.jpg");

        assertThat(first.getOrdinal()).isEqualTo("1st");
        assertThat(second.getOrdinal()).isEqualTo("2nd");
        assertThat(third.getOrdinal()).isEqualTo("3rd");
        assertThat(eleventh.getOrdinal()).isEqualTo("11th");

        assertThat(first.getInitials()).isEqualTo("JP");
        assertThat(third.getInitials()).isEqualTo("PC");
        assertThat(eleventh.getInitials()).isEqualTo("BN");

        assertThat(first.getTermDisplay()).isEqualTo("1973 – 1975");
        assertThat(first.isCurrent()).isFalse();
        assertThat(first.hasPhoto()).isFalse();

        assertThat(eleventh.getTermDisplay()).isEqualTo("2021 – Present");
        assertThat(eleventh.isCurrent()).isTrue();
        assertThat(eleventh.hasPhoto()).isTrue();
        assertThat(eleventh.hasExternalPhotoUrl()).isFalse();
    }

    @Test
    void populatesModelWithCurrentPastAndSummaryStats() {
        PresidentService service = mock(PresidentService.class);
        PresidentEntity current = new PresidentEntity(11, "Brian Neil", 2021, null, "brian-neil.jpg");
        PresidentEntity harry = new PresidentEntity(10, "Harry Eland", 2004, 2021, "harry-eland.jpg");
        PresidentEntity founder = new PresidentEntity(1, "Joe Provenzano", 1973, 1975, null);
        when(service.getAllPresidents()).thenReturn(List.of(current, harry, founder));

        PresidentsView view = new PresidentsView(service);
        ConcurrentModel model = new ConcurrentModel();

        String template = view.getPresidents(model);

        assertThat(template).isEqualTo("presidents-modern");
        assertThat(model.getAttribute("currentPresident")).isEqualTo(current);
        assertThat(model.getAttribute("pastPresidents")).isEqualTo(List.of(harry, founder));
        assertThat(model.getAttribute("longestServing")).isEqualTo(harry);
        assertThat(model.getAttribute("foundingYear")).isEqualTo(1973);
        assertThat((Integer) model.getAttribute("leagueYears")).isGreaterThanOrEqualTo(50);
    }
}
