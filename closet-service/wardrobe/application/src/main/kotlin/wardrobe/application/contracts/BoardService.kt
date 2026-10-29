package wardrobe.application.contracts

import wardrobe.application.contracts.dto.BoardDto
import wardrobe.application.contracts.dto.BoardListDto
import wardrobe.application.contracts.dto.BoardWithItemsDto
import wardrobe.application.contracts.requests.CreateBoardRequest
import wardrobe.application.contracts.requests.RenameBoardRequest
import java.util.UUID

interface BoardService {
    fun create(req: wardrobe.application.contracts.requests.CreateBoardRequest): wardrobe.application.contracts.dto.BoardDto
    fun get(userId: UUID, boardId: UUID): wardrobe.application.contracts.dto.BoardWithItemsDto
    fun list(userId: UUID): wardrobe.application.contracts.dto.BoardListDto
    fun rename(req: wardrobe.application.contracts.requests.RenameBoardRequest): wardrobe.application.contracts.dto.BoardDto
    fun delete(userId: UUID, boardId: UUID)
}