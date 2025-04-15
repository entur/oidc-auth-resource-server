package org.entur.auth.spring.config.mdc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MdcFromToProperties {
    private String from;
    private String to;
}
