package wardrobe.application.contracts.requests

import java.util.UUID

data class RenameBoardRequest(val userId: UUID, val boardId: UUID, val title: String)
