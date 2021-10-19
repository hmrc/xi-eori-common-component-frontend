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

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{CustomsId, ExistingEori}
import uk.gov.hmrc.xieoricommoncomponentfrontend.util.FormUtils.formatInput
import java.time.LocalDate


trait NameIdOrganisationMatch {
  def name: String
  def id: String
}

trait NameOrganisationMatch {
  def name: String
}

case class NameIdOrganisationMatchModel(name: String, id: String) extends NameIdOrganisationMatch

object NameIdOrganisationMatchModel {
  implicit val jsonFormat = Json.format[NameIdOrganisationMatchModel]

  def apply(name: String, id: String): NameIdOrganisationMatchModel =
    new NameIdOrganisationMatchModel(name, formatInput(id))

}

case class NameOrganisationMatchModel(name: String) extends NameOrganisationMatch

object NameOrganisationMatchModel {
  implicit val jsonFormat = Json.format[NameOrganisationMatchModel]
}

trait NameDobMatch {
  def firstName: String

  def middleName: Option[String]

  def lastName: String

  def dateOfBirth: LocalDate
}

case class NameDobMatchModel(firstName: String, middleName: Option[String], lastName: String, dateOfBirth: LocalDate)
  extends NameDobMatch {
  def name: String = s"$firstName ${middleName.getOrElse("")} $lastName"
}

object NameDobMatchModel {
  implicit val jsonFormat = Json.format[NameDobMatchModel]
}

trait NameMatch {
  def name: String
}
case class NameMatchModel(name: String) extends NameMatch

object NameMatchModel {
  implicit val jsonFormat = Json.format[NameMatchModel]
}

trait IdMatch {
  def id: String
}

case class IdMatchModel(id: String) extends IdMatch

object IdMatchModel {
  implicit val jsonFormat = Json.format[IdMatchModel]

  def apply(id: String): IdMatchModel = new IdMatchModel(formatInput(id))
}



case class SubscriptionDetails(
  dateEstablished: Option[LocalDate] = None,
  eoriNumber: Option[String] = None,
  existingEoriNumber: Option[ExistingEori] = None,
  addressDetails: Option[AddressViewModel] = None,
  nameIdOrganisationDetails: Option[NameIdOrganisationMatchModel] = None,
  nameOrganisationDetails: Option[NameOrganisationMatchModel] = None,
  nameDobDetails: Option[NameDobMatchModel] = None,
  nameDetails: Option[NameMatchModel] = None,
  idDetails: Option[IdMatchModel] = None,
  customsId: Option[CustomsId] = None
) {

  def name: String =
    nameIdOrganisationDetails.map(_.name) orElse nameOrganisationDetails.map(_.name) orElse nameDobDetails.map(
      _.name
    ) orElse nameDetails
      .map(_.name) getOrElse (throw new IllegalArgumentException("Name is missing"))

}

object SubscriptionDetails {
  val EuVatDetailsLimit = 5

  implicit val format: Format[SubscriptionDetails] = Json.format[SubscriptionDetails]
}




