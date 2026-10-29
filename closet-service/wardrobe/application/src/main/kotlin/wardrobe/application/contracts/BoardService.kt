package wardrobe.application.contracts

import wardrobe.application.contracts.dto.BoardDto
import wardrobe.application.contracts.dto.BoardListDto
import wardrobe.application.contracts.dto.BoardWithItemsDto
import wardrobe.application.contracts.requests.CreateBoardRequest
import wardrobe.application.contracts.requests.RenameBoardRequest
import java.util.UUID

interface BoardService {
    fun create(req: CreateBoardRequest): BoardDto
    fun get(userId: UUID, boardId: UUID): BoardWithItemsDto
    fun list(userId: UUID): BoardListDto
    fun rename(req: RenameBoardRequest): BoardDto
    fun delete(userId: UUID, boardId: UUID)
}