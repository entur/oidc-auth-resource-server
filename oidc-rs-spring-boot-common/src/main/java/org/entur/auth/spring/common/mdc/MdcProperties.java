package org.entur.auth.spring.common.mdc;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "entur.auth.mdc")
public class MdcProperties {
    private List<MdcFromToProperties> mappings = new ArrayList<>();
}
