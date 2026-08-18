package com.ruan.fincore.dto.recurring;

import java.util.List;

public record GenerateResponse(
        int totalGenerated,
        List<String> descriptions
) {
}
