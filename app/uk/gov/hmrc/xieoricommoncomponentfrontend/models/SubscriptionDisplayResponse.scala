/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.xieoricommoncomponentfrontend.models

import play.api.libs.json.{JsError, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
trait CommonHeader {

  private def dateTimeWritesIsoUtc: Writes[LocalDateTime] = new Writes[LocalDateTime] {

    def writes(d: LocalDateTime): JsValue =
      JsString(
        ZonedDateTime.of(d, ZoneId.of("Europe/London")).withNano(0).withZoneSameInstant(ZoneOffset.UTC).format(
          DateTimeFormatter.ISO_DATE_TIME
        )
      )

  }

  private def dateTimeReadsIso: Reads[LocalDateTime] = new Reads[LocalDateTime] {

    def reads(value: JsValue): JsResult[LocalDateTime] =
      try JsSuccess(
        ZonedDateTime.parse(value.as[String], DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(
          ZoneId.of("Europe/London")
        ).toLocalDateTime
      )
      catch {
        case e: Exception => JsError(s"Could not parse '${value.toString()}' as an ISO date. Reason: $e")
      }

  }

  implicit val dateTimeReads  = dateTimeReadsIso
  implicit val dateTimeWrites = dateTimeWritesIsoUtc
}

case class MessagingServiceParam(paramName: String, paramValue: String)

object MessagingServiceParam {
  implicit val formats = Json.format[MessagingServiceParam]

  val positionParamName = "POSITION"
  val Generate          = "GENERATE"
  val Link              = "LINK"
  val Pending           = "WORKLIST"
  val Fail              = "FAIL"

  val formBundleIdParamName = "ETMPFORMBUNDLENUMBER"
}
case class ResponseCommon(
                           status: String,
                           statusText: Option[String] = None,
                           processingDate: LocalDateTime,
                           returnParameters: Option[List[MessagingServiceParam]] = None
                         )

object ResponseCommon extends CommonHeader {
  val StatusOK         = "OK"
  val StatusNotOK      = "NOT_OK"
  implicit val formats = Json.format[ResponseCommon]
}
case class SubscriptionDisplayResponse(
  responseCommon: ResponseCommon,
  responseDetail: SubscriptionDisplayResponseDetail
)

object SubscriptionDisplayResponse {
  implicit val jsonFormat = Json.format[SubscriptionDisplayResponse]
}