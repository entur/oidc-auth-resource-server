package org.entur.auth.spring.config.mdc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "entur.auth.mdc")
public class MdcProperties {
    private List<MdcFromToProperties> mappings = new ArrayList<>();
}
