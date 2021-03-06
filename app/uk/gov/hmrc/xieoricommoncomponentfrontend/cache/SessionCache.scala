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
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.SubscriptionDisplayResponseDetail
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache.{SubscriptionDisplayMongo, UserAnswers}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.{ContactAddressLookup, PBEAddressLookup}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

sealed case class CachedData(
  eori: Option[String] = None,
  subscriptionDisplay: Option[SubscriptionDisplayMongo] = None,
  addressLookupParams: Option[PBEAddressLookup] = None,
  userAnswers: Option[UserAnswers] = None,
  contactAddressParams: Option[ContactAddressLookup] = None
) {

  def subscriptionDisplayMongo(): Option[SubscriptionDisplayResponseDetail] =
    subscriptionDisplay match {
      case Some(resp) =>
        Some(
          SubscriptionDisplayResponseDetail(
            resp.EORINo,
            resp.CDSFullName,
            resp.CDSEstablishmentAddress,
            resp.contactInformation,
            resp.VATIDs,
            resp.shortName,
            resp.dateOfEstablishment,
            resp.XI_Subscription
          )
        )
      case _ => None
    }

  def getUserAnswers: UserAnswers =
    userAnswers.getOrElse(emptyUserAnswers())

}

object CachedData {
  val eoriKey                              = "eori"
  val subscriptionDisplayKey               = "subscriptionDisplay"
  val addressLookupParamsKey               = "addressLookupParams"
  val contactAddressParamsKey              = "contactAddressParams"
  val userAnswersKey                       = "userAnswers"
  val addressLookupResultsKey              = "addressLookupResult"
  implicit val format: OFormat[CachedData] = Json.format[CachedData]

  def emptyUserAnswers(): UserAnswers =
    UserAnswers(None)

}

@Singleton
class SessionCache @Inject() (appConfig: AppConfig, mongo: ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends CacheMongoRepository("session-cache", appConfig.ttl.toSeconds)(mongo.mongoConnector.db, ec) {

  private val eccLogger: Logger = Logger(this.getClass)

  def sessionId(implicit hc: HeaderCarrier): Id =
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
    createOrUpdate(sessionId, subscriptionDisplayKey, Json.toJson(subscriptionDisplay.toSubscriptionDisplayMongo)) map (
      _ => true
    )

  def saveAddressLookupParams(addressLookupParams: PBEAddressLookup)(implicit hc: HeaderCarrier): Future[Boolean] =
    createOrUpdate(sessionId, addressLookupParamsKey, Json.toJson(addressLookupParams)).map(_ => true)

  def saveContactAddressParams(
    contactAddressParams: ContactAddressLookup
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    createOrUpdate(sessionId, contactAddressParamsKey, Json.toJson(contactAddressParams)).map(_ => true)

  def saveUserAnswers(rdh: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean] =
    createOrUpdate(sessionId, userAnswersKey, Json.toJson(rdh)) map (_ => true)

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
      .recoverWith {
        case _ => Future.successful(None)
      }

  def subscriptionDisplay(implicit hc: HeaderCarrier): Future[Option[SubscriptionDisplayResponseDetail]] =
    getCached[Option[SubscriptionDisplayResponseDetail]](
      sessionId,
      (cachedData, _) => cachedData.subscriptionDisplayMongo()
    )

  def addressLookupParams(implicit hc: HeaderCarrier): Future[Option[PBEAddressLookup]] =
    getCached[Option[PBEAddressLookup]](sessionId, (cachedData, _) => cachedData.addressLookupParams)

  def contactAddressParams(implicit hc: HeaderCarrier): Future[Option[ContactAddressLookup]] =
    getCached[Option[ContactAddressLookup]](sessionId, (cachedData, _) => cachedData.contactAddressParams)

  def userAnswers(implicit hc: HeaderCarrier): Future[UserAnswers] =
    getCached[UserAnswers](sessionId, (cachedData, _) => cachedData.getUserAnswers)

  def remove(implicit hc: HeaderCarrier): Future[Boolean] =
    removeById(sessionId.id) map (x => x.writeErrors.isEmpty && x.writeConcernError.isEmpty)

  def clearAddressLookupParams(implicit hc: HeaderCarrier): Future[Unit] =
    createOrUpdate(sessionId, addressLookupParamsKey, Json.toJson(PBEAddressLookup("", None))).map(_ => ())

  def clearContactAddressParams(implicit hc: HeaderCarrier): Future[Unit] =
    createOrUpdate(sessionId, contactAddressParamsKey, Json.toJson(ContactAddressLookup("", None))).map(_ => ())

}

case class SessionTimeOutException(errorMessage: String) extends NoStackTrace
