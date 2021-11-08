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

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json.toJson
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.cache.model.{Cache, Id}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.mongo.{MongoConnector, MongoSpecSupport}
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.{CachedData, SessionCache}
import uk.gov.hmrc.xieoricommoncomponentfrontend.config.AppConfig
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{EnrolmentResponse, Eori, KeyValue}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{EstablishmentAddress, SubscriptionDisplayResponseDetail, SubscriptionInfoVatId}

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class SessionCacheSpec extends IntegrationTestSpec with MockitoSugar with MongoSpecSupport {

  lazy val appConfig = app.injector.instanceOf[AppConfig]

  private val reactiveMongoComponent = new ReactiveMongoComponent {
    override def mongoConnector: MongoConnector = mongoConnectorForTest
  }
  val sessionCache = new SessionCache(appConfig, reactiveMongoComponent)

  val hc = mock[HeaderCarrier]
  val subscriptionDisplay = SubscriptionDisplayResponseDetail(
    Some("EN123456789012345"),
    "John Doe",
    EstablishmentAddress("house no Line 1", "city name", Some("SE28 1AA"), "ZZ"),
    Some(
      List(SubscriptionInfoVatId(Some("GB"), Some("999999")), SubscriptionInfoVatId(Some("ES"), Some("888888")))
    ),
    Some("Doe"),
    Some(LocalDate.of(1963, 4, 1)),
    Some("XIE9XSDF10BCKEYAX")
  )
  val groupEnrolment =
    List(EnrolmentResponse("HMRC-ATAR-ORG", "Activated", List(KeyValue("EORINumber", "GB123456463324"))))

  val eori = Eori("GB123456463324")

  "Session cache" should {

    "store, fetch and update Subscription Display details correctly" in {
      val sessionId: SessionId = setupSession



      await(sessionCache.saveSubscriptionDisplay(subscriptionDisplay)(hc))

      val expectedJson                     = toJson(CachedData(subscriptionDisplay = Some(subscriptionDisplay.toSubscriptionDisplayMongo())))
      val cache                            = await(sessionCache.findById(Id(sessionId.value)))
      val Some(Cache(_, Some(json), _, _)) = cache
      json mustBe expectedJson

      await(sessionCache.subscriptionDisplay(hc)) mustBe Some(subscriptionDisplay)

      val updatedHolder = subscriptionDisplay.copy(
        shortName = Some("different business name"),
        CDSFullName = "Full Name"
      )

      await(sessionCache.saveSubscriptionDisplay(updatedHolder)(hc))

      val expectedUpdatedJson                     = toJson(CachedData(subscriptionDisplay = Some(updatedHolder.toSubscriptionDisplayMongo())))
      val updatedCache                            = await(sessionCache.findById(Id(sessionId.value)))
      val Some(Cache(_, Some(updatedJson), _, _)) = updatedCache
      updatedJson mustBe expectedUpdatedJson
    }

    "store, fetch and update Group enrolment details correctly" in {
      val sessionId: SessionId = setupSession


      await(sessionCache.saveGroupEnrolment(groupEnrolment)(hc))

      val expectedJson                     = toJson(CachedData(groupEnrolment = Some(groupEnrolment)))
      val cache                            = await(sessionCache.findById(Id(sessionId.value)))
      val Some(Cache(_, Some(json), _, _)) = cache
      json mustBe expectedJson

      await(sessionCache.groupEnrolment(hc)) mustBe Some(groupEnrolment)

      val updatedEnrolment = groupEnrolment.head.copy(service="HMRC-TEST-ORG")

      await(sessionCache.saveGroupEnrolment(List(updatedEnrolment))(hc))

      val expectedUpdatedJson                     = toJson(CachedData(groupEnrolment = Some(List(updatedEnrolment))))
      val updatedCache                            = await(sessionCache.findById(Id(sessionId.value)))
      val Some(Cache(_, Some(updatedJson), _, _)) = updatedCache
      updatedJson mustBe expectedUpdatedJson
    }

    "store, fetch and update Eori details correctly" in {
      val sessionId: SessionId = setupSession

      await(sessionCache.saveEori(eori)(hc))

      val expectedJson                     = toJson(CachedData(eori = Some(eori.id)))
      val cache                            = await(sessionCache.findById(Id(sessionId.value)))
      val Some(Cache(_, Some(json), _, _)) = cache
      json mustBe expectedJson

      await(sessionCache.eori(hc)) mustBe Some(eori.id)

      val updatedEori = Eori("GB123456463329")

      await(sessionCache.saveEori(updatedEori)(hc))

      val expectedUpdatedJson                     = toJson(CachedData(eori = Some(updatedEori.id)))
      val updatedCache                            = await(sessionCache.findById(Id(sessionId.value)))
      val Some(Cache(_, Some(updatedJson), _, _)) = updatedCache
      updatedJson mustBe expectedUpdatedJson
    }

    "remove from the cache" in {
      val sessionId: SessionId = setupSession
      await(sessionCache.saveEori(Eori("GB123456463329"))(hc))

      await(sessionCache.remove(hc))

      val cached = await(sessionCache.findById(Id(sessionId.value)))
      cached mustBe None
    }

    "throw exception when eori requested and not available in cache" in {
      val s = setupSession
      await(sessionCache.insert(Cache(Id(s.value), data = Some(toJson(CachedData())))))

      val caught = intercept[IllegalStateException] {
        await(sessionCache.eori(hc))
      }
      caught.getMessage mustBe s"eori is not cached in data for the sessionId: ${s.value}"
    }

    "throw exception when groupEnrolment requested and not available in cache" in {
      val s = setupSession
      await(sessionCache.insert(Cache(Id(s.value), data = Some(toJson(CachedData())))))

      val caught = intercept[IllegalStateException] {
        await(sessionCache.groupEnrolment(hc))
      }
      caught.getMessage mustBe s"groupEnrolment is not cached in data for the sessionId: ${s.value}"
    }
    "provide default when subscription display details holder not in cache" in {
      val s = setupSession
      await(
        sessionCache.insert(
          Cache(Id(s.value), data = Some(toJson(CachedData(eori = Some(eori.id),groupEnrolment = Some(groupEnrolment)))))
        )
      )

      await(sessionCache.subscriptionDisplay(hc)) mustBe Some(SubscriptionDisplayResponseDetail(None,"",EstablishmentAddress("", "", None, ""),None,None,None,None,None))
    }


  }

  private def setupSession: SessionId = {
    val sessionId = SessionId("sessionId-" + UUID.randomUUID())
    when(hc.sessionId).thenReturn(Some(sessionId))
    sessionId
  }

}
