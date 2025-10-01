from __future__ import annotations

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, validator


class StrictBaseModel(BaseModel):
    class Config:
        extra = "forbid"


class ApiResponse(StrictBaseModel):
    status: str
    message: str


class ClockInRequest(StrictBaseModel):
    workOrderAssemblyId: int
    userId: int
    divisionFK: Literal[1]
    deviceDate: str

    @validator("deviceDate")
    def validate_device_date(cls, value: str) -> str:
        _ensure_iso_datetime(value)
        return value


class ClockOutRequest(StrictBaseModel):
    workOrderCollectionId: int
    quantity: int
    quantityScrapped: int
    scrapReasonPK: int
    complete: bool
    comment: str
    deviceTime: str
    divisionFK: Literal[1]

    @validator("deviceTime")
    def validate_device_time(cls, value: str) -> str:
        _ensure_iso_datetime(value)
        return value


def _ensure_iso_datetime(value: str) -> None:
    try:
        datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError as exc:
        raise ValueError("Value must be a valid ISO 8601 datetime string") from exc
