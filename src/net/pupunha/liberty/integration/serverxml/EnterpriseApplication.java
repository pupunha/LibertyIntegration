package net.pupunha.liberty.integration.serverxml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.pupunha.liberty.integration.serverxml.annotation.Attribute;
import net.pupunha.liberty.integration.serverxml.annotation.TagName;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TagName("enterpriseApplication")
public class EnterpriseApplication {

    @Attribute("id")
    private String id;

    @Attribute("location")
    private String location;

    @Attribute("name")
    private String name;

}
