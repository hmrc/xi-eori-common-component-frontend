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
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.cache.model.{Cache, Id}
import uk.gov.hmrc.cache.repository.CacheMongoRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.CachedData._
import uk.gov.hmrc.xieoricommoncomponentfrontend.config.AppConfig
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{EnrolmentResponse, Eori}
import uk.gov.hmrc.cache._
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache.SubscriptionDisplayMongo
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{EstablishmentAddress, SubscriptionDisplayResponseDetail}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

sealed case class CachedData(
  groupEnrolment: Option[List[EnrolmentResponse]] = None,
  eori: Option[String] = None,
  subscriptionDisplay: Option[SubscriptionDisplayMongo] = None
) {

  def eori(sessionId: Id): String =
    eori.getOrElse(throwException(eoriKey, sessionId))

  def groupEnrolment(sessionId: Id): List[EnrolmentResponse] =
    groupEnrolment.getOrElse(throwException(groupEnrolmentKey, sessionId))

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

  private def throwException(name: String, sessionId: Id) =
    throw new IllegalStateException(s"$name is not cached in data for the sessionId: ${sessionId.id}")

}

object CachedData {
  val groupEnrolmentKey      = "groupEnrolment"
  val eoriKey                = "eori"
  val subscriptionDisplayKey = "subscriptionDisplay"
  implicit val format        = Json.format[CachedData]

  def emptySubscriptionDisplay() =
    SubscriptionDisplayMongo(None, "", EstablishmentAddress("", "", None, ""), None, None, None, None, None)

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

  def saveGroupEnrolment(groupEnrolment: List[EnrolmentResponse])(implicit hc: HeaderCarrier): Future[Boolean] =
    createOrUpdate(sessionId, groupEnrolmentKey, Json.toJson(groupEnrolment)) map (_ => true)

  def saveSubscriptionDisplay(
    subscriptionDisplay: SubscriptionDisplayResponseDetail
  )(implicit hc: HeaderCarrier): Future[Boolean] =
    createOrUpdate(
      sessionId,
      subscriptionDisplayKey,
      Json.toJson(subscriptionDisplay.toSubscriptionDisplayMongo())
    ) map (_ => true)

  private def getCached[T](sessionId: Id, t: (CachedData, Id) => T): Future[Option[T]] =
    findById(sessionId.id).map {
      case Some(Cache(_, Some(data), _, _)) =>
        Json.fromJson[CachedData](data) match {
          case d: JsSuccess[CachedData] => Some(t(d.value, sessionId))
          case _: JsError =>
            eccLogger.error(s"No Session data is cached for the sessionId : ${sessionId.id}")
            throw SessionTimeOutException(s"No Session data is cached for the sessionId : ${sessionId.id}")
        }
      case _ =>
        eccLogger.info(s"No match session id for signed in user with session: ${sessionId.id}")
        None
    }

  def eori(implicit hc: HeaderCarrier): Future[Option[String]] =
    getCached[String](sessionId, (cachedData, id) => cachedData.eori(id))

  def groupEnrolment(implicit hc: HeaderCarrier): Future[Option[List[EnrolmentResponse]]] =
    getCached[List[EnrolmentResponse]](sessionId, (cachedData, id) => cachedData.groupEnrolment(id))

  def subscriptionDisplay(implicit hc: HeaderCarrier): Future[Option[SubscriptionDisplayResponseDetail]] =
    getCached[SubscriptionDisplayResponseDetail](sessionId, (cachedData, _) => cachedData.subscriptionDisplayMongo)

  def remove(implicit hc: HeaderCarrier): Future[Boolean] =
    removeById(sessionId.id) map (x => x.writeErrors.isEmpty && x.writeConcernError.isEmpty)

}

case class SessionTimeOutException(errorMessage: String) extends NoStackTrace
