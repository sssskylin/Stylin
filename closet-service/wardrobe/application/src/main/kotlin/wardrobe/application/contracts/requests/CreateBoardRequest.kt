package wardrobe.application.contracts.requests

import java.util.UUID

data class CreateBoardRequest(val userId: UUID, val title: String)