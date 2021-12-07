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
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.Eori
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache.UserAnswers
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.PBEAddressLookup
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{EstablishmentAddress, SubscriptionDisplayResponseDetail, SubscriptionInfoVatId, XiSubscription}

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class SessionCacheSpec extends IntegrationTestSpec with MockitoSugar with MongoSpecSupport {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private val reactiveMongoComponent = new ReactiveMongoComponent {
    override def mongoConnector: MongoConnector = mongoConnectorForTest
  }
  val sessionCache = new SessionCache(appConfig, reactiveMongoComponent)

  val hc: HeaderCarrier = mock[HeaderCarrier]
  val xiSubscription: XiSubscription = XiSubscription("XI8989989797", Some("7978"))
  val subscriptionDisplay: SubscriptionDisplayResponseDetail = SubscriptionDisplayResponseDetail(
    Some("EN123456789012345"),
    "John Doe",
    EstablishmentAddress("house no Line 1", "city name", Some("SE28 1AA"), "ZZ"),
    Some(
      List(SubscriptionInfoVatId(Some("GB"), Some("999999")), SubscriptionInfoVatId(Some("ES"), Some("888888")))
    ),
    Some("Doe"),
    Some(LocalDate.of(1963, 4, 1)),
    Some(xiSubscription)
  )
  val userAnswers: UserAnswers = UserAnswers(Some(true),Some(true),None,None,None,Some("99976"),Some(true),None)


  val addressLookupParams: PBEAddressLookup = PBEAddressLookup("SE28 1AA", None)

  val eori: Eori = Eori("GB123456463324")

  "Session cache" should {

    "store, fetch and update Subscription Display details correctly" in {
      val sessionId: SessionId = setupSession



      await(sessionCache.saveSubscriptionDisplay(subscriptionDisplay)(hc))

      val expectedJson                     = toJson(CachedData(subscriptionDisplay = Some(subscriptionDisplay.toSubscriptionDisplayMongo)))
      val cache                            = await(sessionCache.findById(Id(sessionId.value)))
      val Some(Cache(_, Some(json), _, _)) = cache
      json mustBe expectedJson

      await(sessionCache.subscriptionDisplay(hc)) mustBe Some(subscriptionDisplay)

      val updatedHolder = subscriptionDisplay.copy(
        shortName = Some("different business name"),
        CDSFullName = "Full Name"
      )

      await(sessionCache.saveSubscriptionDisplay(updatedHolder)(hc))

      val expectedUpdatedJson                     = toJson(CachedData(subscriptionDisplay = Some(updatedHolder.toSubscriptionDisplayMongo)))
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

    "store, fetch and update Address Lookup param details correctly" in {
      val sessionId: SessionId = setupSession



      await(sessionCache.saveAddressLookupParams(addressLookupParams)(hc))

      val expectedJson                     = toJson(CachedData(addressLookupParams = Some(addressLookupParams)))
      val cache                            = await(sessionCache.findById(Id(sessionId.value)))
      val Some(Cache(_, Some(json), _, _)) = cache
      json mustBe expectedJson

      await(sessionCache.addressLookupParams(hc)) mustBe Some(addressLookupParams)

      val updatedHolder = addressLookupParams.copy(
        postcode = "SE28 1AC",
        line1 = Some("line1")
      )

      await(sessionCache.saveAddressLookupParams(updatedHolder)(hc))

      val expectedUpdatedJson                     = toJson(CachedData(addressLookupParams = Some(updatedHolder)))
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

    "return None when eori requested and not available in cache" in {
      val s = setupSession
      await(sessionCache.insert(Cache(Id(s.value), data = Some(toJson(CachedData())))))
      await(sessionCache.eori(hc)) mustBe None
    }

    "return None when subscription display details holder not in cache" in {
      val s = setupSession
      await(
        sessionCache.insert(
          Cache(Id(s.value), data = Some(toJson(CachedData(eori = Some(eori.id)))))
        )
      )

      await(sessionCache.subscriptionDisplay(hc)) mustBe None
    }

    "throw IllegalStateException when session id is not retrieved from hc" in {
      when(hc.sessionId).thenReturn(None)

      val e1 = intercept[IllegalStateException] {
        await(sessionCache.subscriptionDisplay(hc))
      }
      e1.getMessage mustBe "Session id is not available"
    }

    "provide default when registration details holder not in cache" in {
      val s = setupSession
      await(
        sessionCache.insert(
          Cache(Id(s.value), data = Some(toJson(CachedData(eori = Some(eori.id)))))
        )
      )

      await(sessionCache.userAnswers(hc)) mustBe UserAnswers(None)
    }

    "store, fetch and update Registration details correctly" in {
      val sessionId: SessionId = setupSession



      await(sessionCache.saveUserAnswers(userAnswers)(hc))

      val expectedJson                     = toJson(CachedData(userAnswers = Some(userAnswers)))
      val cache                            = await(sessionCache.findById(Id(sessionId.value)))
      val Some(Cache(_, Some(json), _, _)) = cache
      json mustBe expectedJson

      await(sessionCache.userAnswers(hc)) mustBe userAnswers

      val updatedHolder = userAnswers.copy(
        confirmDetails = Some("changeDetails"),
        personalDataDisclosureConsent = Some(true)
      )

      await(sessionCache.saveUserAnswers(updatedHolder)(hc))

      val expectedUpdatedJson                     = toJson(CachedData(userAnswers = Some(updatedHolder)))
      val updatedCache                            = await(sessionCache.findById(Id(sessionId.value)))
      val Some(Cache(_, Some(updatedJson), _, _)) = updatedCache
      updatedJson mustBe expectedUpdatedJson
    }

    }

  private def setupSession: SessionId = {
    val sessionId = SessionId("sessionId-" + UUID.randomUUID())
    when(hc.sessionId).thenReturn(Some(sessionId))
    sessionId
  }

}
