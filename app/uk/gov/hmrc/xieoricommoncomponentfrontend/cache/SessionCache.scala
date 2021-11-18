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

package uk.gov.hmrc.xieoricommoncomponentfrontend.cache

import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Json, OFormat}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.cache._
import uk.gov.hmrc.cache.model.{Cache, Id}
import uk.gov.hmrc.cache.repository.CacheMongoRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.CachedData._
import uk.gov.hmrc.xieoricommoncomponentfrontend.config.AppConfig
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.Eori
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache.{RegistrationDetails, SubscriptionDisplayMongo}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.PBEAddressLookup
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{AddressLookup, EstablishmentAddress, SubscriptionDisplayResponseDetail}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

sealed case class CachedData(
  eori: Option[String] = None,
  subscriptionDisplay: Option[SubscriptionDisplayMongo] = None,
  addressLookupParams: Option[PBEAddressLookup] = None,
  registrationDetails: Option[RegistrationDetails] = None,
  addressLookupResult: Option[Seq[AddressLookup]] = None
) {

/*  def eori(sessionId: Id): String =
    eori.getOrElse(throwException(eoriKey, sessionId))*/

  def subscriptionDisplayMongo(): SubscriptionDisplayResponseDetail = {
    val resp = subscriptionDisplay.getOrElse(emptySubscriptionDisplay())
    SubscriptionDisplayResponseDetail(
      resp.EORINo,
      resp.CDSFullName,
      resp.CDSEstablishmentAddress,
      resp.VATIDs,
      resp.shortName,
      resp.dateOfEstablishment,
      resp.XIEORINo,
      resp.XIVatNo
    )
  }
/*
  def getAddressLookupParams: PBEAddressLookup =
    addressLookupParams.getOrElse(emptyAddressLookupParams())



  def getAddressLookupResult: Seq[AddressLookup] =
    addressLookupResult.getOrElse(None)*/

  def getRegistrationDetails: RegistrationDetails =
    registrationDetails.getOrElse(emptyRegistrationDetails())
/*
  private def throwException(name: String, sessionId: Id) =
    throw new IllegalStateException(s"$name is not cached in data for the sessionId: ${sessionId.id}")
*/

}

object CachedData {
  val eoriKey                              = "eori"
  val subscriptionDisplayKey               = "subscriptionDisplay"
  val addressLookupParamsKey               = "addressLookupParams"
  val registrationDetailsKey               = "registrationDetails"
  val addressLookupResultsKey              = "addressLookupResult"
  implicit val format: OFormat[CachedData] = Json.format[CachedData]

  def emptySubscriptionDisplay(): SubscriptionDisplayMongo =
    SubscriptionDisplayMongo(None, "", EstablishmentAddress("", "", None, ""), None, None, None, None, None)

  def emptyAddressLookupParams(): PBEAddressLookup = PBEAddressLookup("", None)

  def emptyAddressLookupResult(): Seq[AddressLookup] = List(AddressLookup("", "","",""))

  def emptyRegistrationDetails(): RegistrationDetails =
    RegistrationDetails(None)

}

@Singleton
class SessionCache @Inject() (appConfig: AppConfig, mongo: ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends CacheMongoRepository("session-cache", appConfig.ttl.toSeconds)(mongo.mongoConnector.db, ec) {

  private val eccLogger: Logger = Logger(this.getClass)

  private def sessionId(implicit hc: HeaderCarrier): Id =
    hc.sessionId match {
      case None =>
        throw new IllegalStateException("Session id is not available")
      case Some(sessionId) => model.Id(sessionId.value)
    }

  def saveEori(eori: Eori)(implicit hc: HeaderCarrier): Future[Boolean] =
    createOrUpdate(sessionId, eoriKey, Json.toJson(eori.id)) map (_ => true)

  def saveSubscriptionDisplay(
    subscriptionDisplay: SubscriptionDisplayResponseDetail
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    createOrUpdate(
      sessionId,
      subscriptionDisplayKey,
      Json.toJson(subscriptionDisplay.toSubscriptionDisplayMongo())
    ) map (_ => true)

  def saveAddressLookupParams(addressLookupParams: PBEAddressLookup)(implicit hc: HeaderCarrier): Future[Boolean] =
    createOrUpdate(sessionId, addressLookupParamsKey, Json.toJson(addressLookupParams)).map(_ => true)

  def saveRegistrationDetails(rdh: RegistrationDetails)(implicit hc: HeaderCarrier): Future[Boolean] =
    createOrUpdate(sessionId, registrationDetailsKey, Json.toJson(rdh)) map (_ => true)

  def saveAddressLookupResult(addressLookup: Seq[AddressLookup])(implicit hc: HeaderCarrier): Future[Boolean] =
    createOrUpdate(sessionId, addressLookupResultsKey, Json.toJson(addressLookup)).map(_ => true)

  private def getCached[T](sessionId: Id, t: (CachedData, Id) => T): Future[T] =
    findById(sessionId.id).map {
      case Some(Cache(_, Some(data), _, _)) =>
        Json.fromJson[CachedData](data) match {
          case d: JsSuccess[CachedData] => t(d.value, sessionId)
          case _: JsError =>
            eccLogger.error(s"No Session data is cached for the sessionId : ${sessionId.id}")
            throw SessionTimeOutException(s"No Session data is cached for the sessionId : ${sessionId.id}")
        }
      case _ =>
        eccLogger.info(s"No match session id for signed in user with session: ${sessionId.id}")
        throw SessionTimeOutException(s"No match session id for signed in user with session : ${sessionId.id}")
    }

  def eori(implicit hc: HeaderCarrier): Future[Option[String]] =
    getCached[Option[String]](sessionId, (cachedData, _) => cachedData.eori)

  def subscriptionDisplay(implicit hc: HeaderCarrier): Future[Option[SubscriptionDisplayResponseDetail]] =
    getCached[Option[SubscriptionDisplayResponseDetail]](sessionId, (cachedData, _) => Some(cachedData.subscriptionDisplayMongo()))

  def addressLookupParams(implicit hc: HeaderCarrier): Future[Option[PBEAddressLookup]] =
    getCached[Option[PBEAddressLookup]](sessionId, (cachedData, _) => cachedData.addressLookupParams)

  def addressLookupResult(implicit hc: HeaderCarrier): Future[Option[Seq[AddressLookup]]] =
    getCached[Option[Seq[AddressLookup]]](sessionId, (cachedData, _) => cachedData.addressLookupResult)

  def registrationDetails(implicit hc: HeaderCarrier): Future[RegistrationDetails] = {
   getCached[RegistrationDetails](sessionId, (cachedData, _) => cachedData.getRegistrationDetails)
  }

  def tradeWithNI(implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    getCached[RegistrationDetails](sessionId, (cachedData, _) => cachedData.getRegistrationDetails)
      .map(_.tradeWithNI)
      .recoverWith {
        case _ => Future.successful(None)
       }
  }

  def remove(implicit hc: HeaderCarrier): Future[Boolean] =
    removeById(sessionId.id) map (x => x.writeErrors.isEmpty && x.writeConcernError.isEmpty)
  }

case class SessionTimeOutException(errorMessage: String) extends NoStackTrace
