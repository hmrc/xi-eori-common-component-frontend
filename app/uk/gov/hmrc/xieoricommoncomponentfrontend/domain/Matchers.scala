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

package uk.gov.hmrc.xieoricommoncomponentfrontend.domain

import play.api.libs.json._

sealed trait CustomsId {
  def id: String
}

case class Utr(override val id: String) extends CustomsId

case class Eori(override val id: String) extends CustomsId

case class Nino(override val id: String) extends CustomsId

case class SafeId(override val id: String) extends CustomsId

case class TaxPayerId(override val id: String) extends CustomsId {
  private val MDGTaxPayerIdLength = 42
  val mdgTaxPayerId: String       = id + "0" * (MDGTaxPayerIdLength - id.length)
}

object TaxPayerId {
  implicit val format = Json.format[TaxPayerId]
}

object SafeId {
  implicit val format                                = Json.format[SafeId]
  implicit def toJsonFormat(safeId: SafeId): JsValue = Json.toJson(safeId)
}

case class InternalId(id: String)

object InternalId {

  def apply(id: Option[String]): InternalId =
    new InternalId(id.getOrElse(throw new IllegalArgumentException("InternalId is missing")))

  implicit val format = Json.format[InternalId]
}

case class GroupId(id: String)

object GroupId {

  def apply(id: Option[String]): GroupId =
    new GroupId(id.getOrElse(throw new IllegalArgumentException("GroupId is missing")))

  implicit val format = Json.format[GroupId]
}

object CustomsId {
  private val utr        = "utr"
  private val eori       = "eori"
  private val nino       = "nino"
  private val safeId     = "safeId"
  private val taxPayerId = "taxPayerId"

  private val idTypeMapping = Map[String, String => CustomsId](
    utr        -> Utr,
    eori       -> Eori,
    nino       -> Nino,
    safeId     -> (s => SafeId(s)),
    taxPayerId -> (s => TaxPayerId(s))
  )

  implicit val formats = Format[CustomsId](
    fjs = Reads { js =>
      idTypeMapping.view.flatMap {
        case (jsFieldName, idConstruct) =>
          for (id <- (js \ jsFieldName).asOpt[String]) yield idConstruct(id)
      }.headOption
        .fold[JsResult[CustomsId]](JsError("No matching id type and value found"))(customsId => JsSuccess(customsId))
    },
    tjs = Writes {
      case Utr(id)        => Json.obj(utr -> id)
      case Eori(id)       => Json.obj(eori -> id)
      case Nino(id)       => Json.obj(nino -> id)
      case SafeId(id)     => Json.obj(safeId -> id)
      case TaxPayerId(id) => Json.obj(taxPayerId -> id)
    }
  )

  def apply(idType: String, idNumber: String): CustomsId =
    idType match {
      case "NINO"   => Nino(idNumber)
      case "UTR"    => Utr(idNumber)
      case "EORI"   => Eori(idNumber)
      case "SAFEID" => SafeId(idNumber)
      case _        => throw new IllegalArgumentException(s"Unknown Identifier $idType")
    }

}

case class ExistingEori(id: String, enrolmentKey: String)

object ExistingEori {
  implicit val jsonFormat = Json.format[ExistingEori]

  def apply(id: Option[String], enrolmentKey: String): ExistingEori =
    new ExistingEori(id.getOrElse(throw new IllegalArgumentException("EORI is missing")), enrolmentKey)

}
