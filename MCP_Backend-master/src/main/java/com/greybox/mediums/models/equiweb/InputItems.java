package com.greybox.mediums.models.equiweb;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class InputItems {
    private List<Item> items;
}
