package com.islam24.api.error.exception

import java.util.UUID

class UserNotFoundException(id: UUID? = null) : RuntimeException("User not found with id: $id")