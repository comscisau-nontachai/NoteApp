package nontachai.becomedev.noteapp


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.fragment_ads.*

class AdsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAds()
    }
    private fun showAds(){
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        //my App ID : ca-app-pub-1787292132881960~8520030186
        MobileAds.initialize(context, "ca-app-pub-1787292132881960~8520030186")
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}
