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

package cache

import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.cache.model.Id
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.CachedData
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.PBEAddressLookup
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{EstablishmentAddress, SubscriptionDisplayResponseDetail}
import util.BaseSpec

class SessionCacheSpec extends BaseSpec with MockitoSugar {

  val sessionId: Id = Id("1234567")

  def errorMsg(name: String) = s"$name is not cached in data for the sessionId: ${sessionId.id}"

  "CachedData" should {

    "throw IllegalStateException" when {

      "eori missing " in {
        CachedData().eori shouldBe None
      }

    }

    "return default" when {
      val emptySubscriptionDisplay =
        SubscriptionDisplayResponseDetail(
          None,
          "",
          EstablishmentAddress("", "", None, ""),
          None,
          None,
          None,
          None,
          None
        )
      val emptyAddressLookupParams = PBEAddressLookup("", None)

      "subscriptionDisplay missing " in {
        CachedData().subscriptionDisplayMongo() shouldBe emptySubscriptionDisplay
      }

      "addressLookupParams missing " in {
        CachedData().addressLookupParams shouldBe None
      }
    }

  }

}
