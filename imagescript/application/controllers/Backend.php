<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of Backend
 *
 * @author silve
 */
require 'HZip.php';

class Backend extends CI_Controller {
 
    const MAX_PASSWORD_LENGTH = 4096;
    var $host_name = "http://omcsa.org";
    var $image_base_url = "http://omcsa.org/bundles/swarminfoimagescript/ressources/";
    var $algorithm = "sha512";
    var $encodeHashAsBase64 = true;
    var $iterations  = 5000;
    
    public function login()
    {
        //input values
        $username = $this->input->post('username');
        $password = $this->input->post('password');
        $this->load->database();
        $sql = "select * from user where username='".$username."' OR email='".$username."'";
        $query = $this->db->query($sql);
        $result = $query->result();
        if(count($result) > 0)
        {
            $encoded_password = $this->encodePassword($password, $result[0]->salt);
            if($encoded_password == $result[0]->password)
            {
                $id = $result[0]->id;
                $email = $result[0]->email;
                $username = $result[0]->username;
                $sql = "select A.*, B.expire from fieldsregister A, user B where A.userid=".$id." and A.userid = B.id";
                $query = $this->db->query($sql);
                $result = $query->result();
                $data = $result[0];
                $data->email = $email;
                $data->username = $username;
                echo '{"result":1, "message":"Login Success", "data":'.json_encode($data)."}";
            }
            else
            {
                echo '{"result":0, "message":"Login Failed. Please input correct Username or email or password"}';
            }
        }
        else
        {
            echo '{"result":0, "message":"Login Failed. Please input correct Username or email or password"}';
        }
    }
    
    private function mergePasswordAndSalt($password, $salt)
    {
        if (empty($salt)) {
            return $password;
        }

        if (false !== strrpos($salt, '{') || false !== strrpos($salt, '}')) {
            return password;
        }

        return $password.'{'.$salt.'}';
    }
    
    protected function isPasswordTooLong($password)
    {
        return strlen($password) > self::MAX_PASSWORD_LENGTH;
    }
    
    private function encodePassword($raw, $salt)
    {
        if ($this->isPasswordTooLong($raw)) {
            return false;
        }
        if (!\in_array($this->algorithm, hash_algos(), true)) {
            return false;
        }
        $salted = $this->mergePasswordAndSalt($raw, $salt);
        $digest = hash($this->algorithm, $salted, true);
        // "stretch" hash
        for ($i = 1; $i < $this->iterations; ++$i) {
            $digest = hash($this->algorithm, $digest.$salted, true);
        }
        return $this->encodeHashAsBase64 ? base64_encode($digest) : bin2hex($digest);
    }
    
    private function getPostCURL($url, $params = null)
    {
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_POST, true);
        if($params != null)
        {
            curl_setopt($ch, CURLOPT_POSTFIELDS, $params);
        }
        $result = curl_exec ( $ch );
        curl_close($ch);
        return $result;
    }
    
    public function register()
    {
        
    }
    
    public function forget_password()
    {
        $username = $this->input->post('username');
        $this->load->database();
        $sql = "select * from user where username='".$username."' OR email='".$username."'";
        $query = $this->db->query($sql);
        $result = $query->result();
        if(count($result) > 0)
        {
            $params = array("username"=>$username);
            $this->getPostCURL($this->host_name."/resetting/send-email", $params);
            echo '{"result":1, "message":"Please check your email to reset password."}';
        }
        else
        {
            echo '{"result":0, "message":"User does not exist."}';
        }
    }
    
    public function update_profile()
    {
        $id = $this->input->get('id');
        $title = $this->input->get('title');
        $firstname = $this->input->get('firstname');
        $lastname = $this->input->get('lastname');
        $profession = $this->input->get('profession');
        
        $country = $this->input->get('country');
        $address_line_1 = $this->input->get('address_line_1');
        $address_line_2 = $this->input->get('address_line_2');
        $city = $this->input->get('city');
        $postcode = $this->input->get('post_code');
        $region = $this->input->get('region');
        $phone = $this->input->get('phone');
        $promocode = $this->input->get('promocode');
        
        if(!isset($postcode)) 
        {
            $postcode = 0;
        }
        $sql = "update fieldsregister set title='".$title."', firstname='".$firstname."', lastname='".$lastname."',"
                . "profession='".$profession."', country='".$country."', address_line_1='".$address_line_1."', address_line_2='".$address_line_2."',"
                . "city='".$city."', postcode=".$postcode.", region='".$region."', phone='".$phone."', promocode='".$promocode."' where userid=".$id;
        $this->load->database();
        $this->db->query($sql);
        echo '{"result":1, "message":"Sucessfully updated"}';
    }
    
    public function update_userinfo()
    {
        $id = $this->input->post('id');
        $new_password = $this->input->post('new_password');
        $old_password = $this->input->post('old_password');
        $username = $this->input->post('username');
        $email = $this->input->post('email');
        
        
        if(!isset($id) || !isset($new_password) || !isset($old_password) || !isset($username) || !isset($email)
                || $id == "" || $new_password == "" || $old_password == "" || $username == "" || $email == "")
        {
            echo '{"result":0, "message":"Please set all information"}';
            return;
        }
        
        //UserName and Email Check
        $this->load->database();
        $query = $this->db->query("select * from user where id != ".$id. " and (username='".$username."' or email='".$email."')");
        $result = $query->result();
                
        if(count($result) > 0)
        {
            echo '{"result":0, "message":"Email or Username has duplicated."}';
            return;
        }
        
        $query = $this->db->query("select * from user where id=".$id);
        $result = $query->result();
        if(count($result) == 0){
            echo '{"result":0, "message":"Invalid User Id"}';
            return;
        }
        
        $encoded_old_password = $this->encodePassword($old_password, $result[0]->salt);
        if($encoded_old_password != $result[0]->password)
        {
            echo '{"result":0, "message":"Old Password is incorrect"}';
            return;
        }
        
        $encoded_new_password = $this->encodePassword($new_password, $result[0]->salt);
        
        $this->db->query("update user set username='".$username."', username_canonical='".$username."', email='".$email."', email_canonical='".$email."', password='".$encoded_new_password."' where id=".$id);
        echo '{"result":1, "message":"Profile Updated Successfully"}';
    }
     
    public function get_order_history()
    {
        $user_id = $this->input->get('id');
        $this->load->database();
        $query = $this->db->query("select * from orderlist where userid=".$user_id);
        $result = $query->result();
        echo '{"result":1, "data":'.json_encode($result)."}";
    }
    
    public function get_main_categories()
    {
        $search = $this->input->get('search');
        $lang = $this->input->get('lang');
        if(!isset($search))
        {
            $search = "";
        }
        
        if(!isset($lang))
        {
            $lang = "english";
        }
        
        $this->load->database();
        $sql = "select * from  project";
        $query = $this->db->query($sql);
        $result = $query->result();
        
        $tag_map = array();       
        $tagSeq = ["Head" => 0, "spine" => 2, "upper abdomen" => 4, "Upper limb" => 5, "lower limb" => 6, "thorax" => 3, "neck & lower face" => 1];
        //Collect Tag
        $resultData = array();
        for($i = 0; $i < count($result); $i++)
        {
            $data = json_decode($result[$i]->data);
            if(isset($data->general->tag) && $this->isSearchedProject($data, $search, $lang))
            {
                $tag = $data->general->tag;
                if(!isset($tag_map[$tag]))
                {
                    $tag_map[$tag] = new \stdClass();
                    $tag_map[$tag]->count = 0;
                    $tag_map[$tag]->name = $tag;
                    $tag_map[$tag]->data = array();
                    $resultData[(int)$tagSeq[$tag]] = $tag_map[$tag];
                    //array_push($resultData, $tag_map[$tag]);
                }
                $tag_map[$tag]->count++;
                $data->general->logo_image_url = /*$this->image_base_url.*/"/".$data->id."/".$data->general->logo;
                $data->general->id = $data->id;
                $data->general->image_count = $this->getImageCount($data, $search, $lang);
                array_push($tag_map[$tag]->data, $data->general);
            }
        }
        $data = array();
        for($i = 0; $i < 7; $i++)
        {
            array_push($data, $resultData[$i]);
        }
        /*$result = array();
        foreach($tag_map as $data)
        {
            array_push($result, $data);
        }*/
        echo '{"result":1, "data":'.json_encode($data).'}';
    }
    
    private function getImageCount($data, $search, $lang)
    {
        if($search == "") return count($data->images);
        $count = 0;
        $legends = (array)($data->legends);
        foreach($data->images as $image)
        {
            if(isset($image->legends))
            {
                foreach($image->legends as $id => $legend)
                {
                    $sel_legend = null;
                    foreach($data->legends as $item_id => $item)
                    {
                        if($item_id == $id)
                        {
                            $sel_legend = $item;
                            break;
                        }
                    }
                    
                    if($sel_legend != null)
                    {
                        $texts = (array)($sel_legend->text);
                        $text = $texts[$lang];
                        if(strstr($text, $search) != null)
                        {
                            $count++;
                            break;
                        }
                    }
                }
            }
            
        }
        return $count;
    }
    
    private function isSearchedProject($data, $search, $lang)
    {
        if($search == "") return true;
        $legends = $data->legends;
        foreach($legends as $legend)
        {
            $texts = (array)$legend->text;
            $text = $texts[$lang];
            if(strstr($text, $search) != null)
            {
                return true;
            }
        }
        return false;
    }
    
    public function get_languages()
    {
        $this->load->database();
        $sql = "select * from  project limit 1";
        $query = $this->db->query($sql);
        $result = $query->result();
        
        $data = json_decode($result[0]->data);
        $langs = $data->languages;
        $result = array();
        foreach($langs as $lang)
        {
            array_push($result, $lang);
        }
        
        echo '{"result":1, "data":'.json_encode($result).'}';
    }
    
    public function get_all_legends()
    {   
        $lang = $this->input->get('lang');
        if(!isset($lang))
        {
            $lang = "english";
        }
        $this->load->database();
        $sql = "select * from  project";
        $query = $this->db->query($sql);
        $result = $query->result();
        
        $legends_map = array();
        for($i = 0; $i < count($result); $i++)
        {
            $data = json_decode($result[$i]->data);
            foreach($data->legends as $id => $legends)
            {
                if(!isset($legends_map[$id]))
                {
                    $legends_map[$id] = $legends;
                }
            }
        }
        $legends_array = array();
        foreach($legends_map as $id=>$legends)
        {
            $texts = (array)($legends->text);
            array_push($legends_array, $texts[$lang]);
        }
        
        echo '{"result":1, "data":'.json_encode($legends_array).'}';
    }
    
    private function makeAllProjectContent()
    {
        $this->load->database();
        
        //rmdir(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/json");
        mkdir(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/json/");
        
        $sql = "select * from project";
        $query = $this->db->query($sql);
        $result = $query->result();
        if(count($result) == 0)
        {
            echo '{"result":0, "empty result"}';
            return false;
        }
        
        for($i = 0; $i < count($result); $i++)
        {
            if(strstr($result[$i]->data, '"publish":"publish"') == false) 
            {
                continue;
            }
            $json_path = APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/json/".$result[$i]->id.".json";
            $data = json_decode($result[$i]->data);
            
            foreach($data->images as $image)
            {
                $image->url = substr($image->url, strlen("/bundles/swarminfoimagescript/ressources"));
            }
            
            //Check Legends Description
            foreach($data->legends as $item)
            {               
                if(!isset($item->description) || is_string($item->description) || !isset($item->description->en))
                {
                    $description = "";
                    if(isset($item->description))
                    {
                        $description = $item->description;
                    }
                    
                    if(is_string($description))
                    {
                        $item->description = new \stdClass();
                        $item->description->en = $description;
                        $item->description->fr = $description;
                        $item->description->ja = $description;
                        $item->description->sc = $description;
                        $item->description->tc = $description;
                        $item->description->es = $description;
                        $item->description->pt = $description;
                    }
                    else
                    {
                        $item->description = new \stdClass();
                        $item->description->en = "";
                        $item->description->fr = "";
                        $item->description->ja = "";
                        $item->description->sc = "";
                        $item->description->tc = "";
                        $item->description->es = "";
                        $item->description->pt = "";
                    }
                }
            }
            file_put_contents($json_path, json_encode($data));
        }
        
        /*for($i = 0; $i < count($result); $i++)
        {
            $data = json_decode($result[$i]->data);
            $images = array();
            if(!isset($data->general) || !isset($data->general->publish) || $data->general->publish != 'publish') continue;
            foreach($data->images as $image)
            {
                //$image->url = $this->host_name.$image->url;
                $image->url = substr($image->url, strlen("/bundles/swarminfoimagescript/ressources"));
                unset($image->position);
                unset($image->series);
                $legends = array();
                foreach($image->legends as $legend)
                {
                    $selLegend = null;
                    foreach($data->legends as $item)
                    {
                        if($legend->id == $item->id)
                        {
                            $selLegend = $item;
                            break;
                        }
                    }
                    array_push($legends, $legend);
                }
                if(count($legends) > 0)
                {
                    $image->legends = $legends;
                    array_push($images, $image);
                }
            }
            $data->images = $images;
            
            $legends = array();
            $structure_map = array();
            foreach($data->legends as $item)
            {               
                array_push($legends, $item);
                $structure_map[$item->structure] = 1;
            }
            $data->legends = $legends;
            
            $structures = array();
            foreach($data->structures as $structure)
            {
                if(isset($structure_map[$structure->id]))
                {
                    array_push($structures, $structure);
                }
            }
            $data->structures = $structures;
            unset($data->languages);
            //unset($data->general);
            unset($data->series);
            
            array_push($dataArray, $data);
        }*/
        
        //
        
        return true;
    }
    
    public function get_project_content()
    {   
        $id = $this->input->get('id');
        $search = $this->input->get('search');
        $lang = $this->input->get('lang');
        $this->load->database();
        if(!isset($id))
        {
            $query = $this->db->query("select * from manage_version order by id desc limit 1");
            $result = $query->result();
            $zipname = $result[0]->modified_date."_project.zip";
            $zipPath = APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/".$zipname;
            if(!file_exists($zipPath))
            {
                //Make Zip file
                if($this->makeAllProjectContent())
                {
                    HZip::zipDir(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/json", $zipPath);
                    $this->delete_directory(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/json");
                }
                else
                {
                    return;
                }
            }
            header('Content-Type: application/zip');
            header('Content-disposition: attachment; filename='.$zipname);
            header('Content-Length: ' . filesize($zipPath));
            readfile($zipPath);
            return;
        }
        /*if(!isset($id))
        {
            echo '{"result":0, "message":"id not set"}';
            return;
        }*/
        
        if(!isset($search))
        {
            $search = "";
        }
        
        if(!isset($lang))
        {
            $lang = "english";
        }
        
        $sql = "select * from project where id=".$id;
        $query = $this->db->query($sql);
        $result = $query->result();
        if(count($result) == 0)
        {
            echo '{"result":0, "empty result"}';
            return;
        }
        $data = json_decode($result[0]->data);
        $result = array();
        if($this->isSearchedProject($data, $search, $lang))
        {
            $images = array();
            foreach($data->images as $image)
            {
                //$image->url = $this->host_name.$image->url;
                $image->url = substr($image->url, strlen("/bundles/swarminfoimagescript/ressources"));
                unset($image->position);
                unset($image->series);
                $legends = array();
                foreach($image->legends as $legend)
                {
                    $selLegend = null;
                    foreach($data->legends as $item)
                    {
                        if($legend->id == $item->id)
                        {
                            $selLegend = $item;
                            break;
                        }
                    }
                    
                    if($this->isSearchLegend($item, $search, $lang))
                    {
                        array_push($legends, $legend);
                    }
                }
                if(count($legends) > 0)
                {
                    $image->legends = $legends;
                    array_push($images, $image);
                }
            }
            $data->images = $images;
            
            $legends = array();
            $structure_map = array();
            foreach($data->legends as $item)
            {
                if($this->isSearchLegend($item, $search, $lang))
                {
                    array_push($legends, $item);
                    $structure_map[$item->structure] = 1;
                }
            }
            $data->legends = $legends;
            
            $structures = array();
            foreach($data->structures as $structure)
            {
                if(isset($structure_map[$structure->id]))
                {
                    array_push($structures, $structure);
                }
            }
            $data->structures = $structures;
            unset($data->languages);
            //unset($data->general);
            unset($data->series);
            
            echo '{"result":1, "data":'.json_encode($data).'}';
        }
        else
        {
            echo '{"result":0, "Search Result Empty"}';
        }
    }
    
    private function isSearchLegend($legend, $search, $lang)
    {
        if($search == "") return true;
        $texts = (array)$legend->text;
        $text = $texts[$lang];
        if(strstr($text, $search) != null)
        {
            return true;
        }
        return false;
    }
    
    public function get_title()
    {
        echo '{"result":1, "data":["Dr.", "Prof.", "Mr.", "Mrs.", "Ms."]}';
    }
    
    public function get_profession()
    {
        echo '{"result":1, "data":["Radiologist", "Radiology trainee", "Radiographer", "Radiation oncologist", "Orthopaedics surgeon", "Clinical oncologist", "Rheumatologist", "General practitioner", "Junior doctor", "Other medical specialist", "Chiropractor", "Nursing", "Physiotherapist", "Other Therapist/technologist", "Other healthcare professional", "Student", "Professor/lecturer/other educator", "Managerial/administrative staff", "Others"]}';
    }
    
    public function get_country()
    {
        echo '{"result":1, "data":[{"country_code":"AF", "country_name":"Afghanistan"},{"country_code":"AL", "country_name":"Albania"},{"country_code":"DZ", "country_name":"Algeria"},{"country_code":"AS", "country_name":"American Samoa"},{"country_code":"AD", "country_name":"Andorra"},{"country_code":"AO", "country_name":"Angola"},{"country_code":"AI", "country_name":"Anguilla"},{"country_code":"AQ", "country_name":"Antarctica"},{"country_code":"AG", "country_name":"Antigua and Barbuda"},{"country_code":"AR", "country_name":"Argentina"},{"country_code":"AM", "country_name":"Armenia"},{"country_code":"AW", "country_name":"Aruba"},{"country_code":"AU", "country_name":"Australia"},{"country_code":"AT", "country_name":"Austria"},{"country_code":"AZ", "country_name":"Azerbaijan"},{"country_code":"BS", "country_name":"Bahamas"},{"country_code":"BH", "country_name":"Bahrain"},{"country_code":"BD", "country_name":"Bangladesh"},{"country_code":"BB", "country_name":"Barbados"},{"country_code":"BY", "country_name":"Belarus"},{"country_code":"BE", "country_name":"Belgium"},{"country_code":"BZ", "country_name":"Belize"},{"country_code":"BJ", "country_name":"Benin"},{"country_code":"BM", "country_name":"Bermuda"},{"country_code":"BT", "country_name":"Bhutan"},{"country_code":"BO", "country_name":"Bolivia"},{"country_code":"BA", "country_name":"Bosnia and Herzegovina"},{"country_code":"BW", "country_name":"Botswana"},{"country_code":"BV", "country_name":"Bouvet Island"},{"country_code":"BR", "country_name":"Brazil"},{"country_code":"IO", "country_name":"British Indian Ocean Territory"},{"country_code":"BN", "country_name":"Brunei Darussalam"},{"country_code":"BG", "country_name":"Bulgaria"},{"country_code":"BF", "country_name":"Burkina Faso"},{"country_code":"BI", "country_name":"Burundi"},{"country_code":"KH", "country_name":"Cambodia"},{"country_code":"CM", "country_name":"Cameroon"},{"country_code":"CA", "country_name":"Canada"},{"country_code":"CV", "country_name":"Cape Verde"},{"country_code":"KY", "country_name":"Cayman Islands"},{"country_code":"CF", "country_name":"Central African Republic"},{"country_code":"TD", "country_name":"Chad"},{"country_code":"CL", "country_name":"Chile"},{"country_code":"CN", "country_name":"China"},{"country_code":"CX", "country_name":"Christmas Island"},{"country_code":"CC", "country_name":"Cocos (Keeling) Islands"},{"country_code":"CO", "country_name":"Colombia"},{"country_code":"KM", "country_name":"Comoros"},{"country_code":"CG", "country_name":"Congo"},{"country_code":"CD", "country_name":"Congo, The Democratic Republic Of The"},{"country_code":"CK", "country_name":"Cook Islands"},{"country_code":"CR", "country_name":"Costa Rica"},{"country_code":"HR", "country_name":"Croatia"},{"country_code":"CU", "country_name":"Cuba"},{"country_code":"CY", "country_name":"Cyprus"},{"country_code":"CZ", "country_name":"Czech Republic"},{"country_code":"CI", "country_name":"Côte d\'Ivoire"},{"country_code":"DK", "country_name":"Denmark"},{"country_code":"DJ", "country_name":"Djibouti"},{"country_code":"DM", "country_name":"Dominica"},{"country_code":"DO", "country_name":"Dominican Republic"},{"country_code":"EC", "country_name":"Ecuador"},{"country_code":"EG", "country_name":"Egypt"},{"country_code":"SV", "country_name":"El Salvador"},{"country_code":"GQ", "country_name":"Equatorial Guinea"},{"country_code":"ER", "country_name":"Eritrea"},{"country_code":"EE", "country_name":"Estonia"},{"country_code":"ET", "country_name":"Ethiopia"},{"country_code":"FK", "country_name":"Falkland Islands (Malvinas)"},{"country_code":"FO", "country_name":"Faroe Islands"},{"country_code":"FJ", "country_name":"Fiji"},{"country_code":"FI", "country_name":"Finland"},{"country_code":"FR", "country_name":"France"},{"country_code":"GF", "country_name":"French Guiana"},{"country_code":"PF", "country_name":"French Polynesia"},{"country_code":"TF", "country_name":"French Southern Territories"},{"country_code":"GA", "country_name":"Gabon"},{"country_code":"GM", "country_name":"Gambia"},{"country_code":"GE", "country_name":"Georgia"},{"country_code":"DE", "country_name":"Germany"},{"country_code":"GH", "country_name":"Ghana"},{"country_code":"GI", "country_name":"Gibraltar"},{"country_code":"GR", "country_name":"Greece"},{"country_code":"GL", "country_name":"Greenland"},{"country_code":"GD", "country_name":"Grenada"},{"country_code":"GP", "country_name":"Guadeloupe"},{"country_code":"GU", "country_name":"Guam"},{"country_code":"GT", "country_name":"Guatemala"},{"country_code":"GG", "country_name":"Guernsey"},{"country_code":"GN", "country_name":"Guinea"},{"country_code":"GW", "country_name":"Guinea-Bissau"},{"country_code":"GY", "country_name":"Guyana"},{"country_code":"HT", "country_name":"Haiti"},{"country_code":"HM", "country_name":"Heard Island and McDonald Islands"},{"country_code":"VA", "country_name":"Holy See (Vatican City State)"},{"country_code":"HN", "country_name":"Honduras"},{"country_code":"HK", "country_name":"Hong Kong"},{"country_code":"HU", "country_name":"Hungary"},{"country_code":"IS", "country_name":"Iceland"},{"country_code":"IN", "country_name":"India"},{"country_code":"ID", "country_name":"Indonesia"},{"country_code":"IR", "country_name":"Iran, Islamic Republic of"},{"country_code":"IQ", "country_name":"Iraq"},{"country_code":"IE", "country_name":"Ireland"},{"country_code":"IM", "country_name":"Isle of Man"},{"country_code":"IL", "country_name":"Israel"},{"country_code":"IT", "country_name":"Italy"},{"country_code":"JM", "country_name":"Jamaica"},{"country_code":"JP", "country_name":"Japan"},{"country_code":"JE", "country_name":"Jersey"},{"country_code":"JO", "country_name":"Jordan"},{"country_code":"KZ", "country_name":"Kazakhstan"},{"country_code":"KE", "country_name":"Kenya"},{"country_code":"KI", "country_name":"Kiribati"},{"country_code":"KP", "country_name":"Korea, Democratic People\'s Republic of"},{"country_code":"KR", "country_name":"Korea, Republic of"},{"country_code":"KW", "country_name":"Kuwait"},{"country_code":"KG", "country_name":"Kyrgyzstan"},{"country_code":"LA", "country_name":"Lao People\'s Democratic Republic"},{"country_code":"LV", "country_name":"Latvia"},{"country_code":"LB", "country_name":"Lebanon"},{"country_code":"LS", "country_name":"Lesotho"},{"country_code":"LR", "country_name":"Liberia"},{"country_code":"LY", "country_name":"Libyan Arab Jamahiriya"},{"country_code":"LI", "country_name":"Liechtenstein"},{"country_code":"LT", "country_name":"Lithuania"},{"country_code":"LU", "country_name":"Luxembourg"},{"country_code":"MO", "country_name":"Macau"},{"country_code":"MK", "country_name":"Macedonia, The Former Yugoslav Republic of"},{"country_code":"MG", "country_name":"Madagascar"},{"country_code":"MW", "country_name":"Malawi"},{"country_code":"MY", "country_name":"Malaysia"},{"country_code":"MV", "country_name":"Maldives"},{"country_code":"ML", "country_name":"Mali"},{"country_code":"MT", "country_name":"Malta"},{"country_code":"MH", "country_name":"Marshall Islands"},{"country_code":"MQ", "country_name":"Martinique"},{"country_code":"MR", "country_name":"Mauritania"},{"country_code":"MU", "country_name":"Mauritius"},{"country_code":"YT", "country_name":"Mayotte"},{"country_code":"MX", "country_name":"Mexico"},{"country_code":"FM", "country_name":"Micronesia, Federated States of"},{"country_code":"MD", "country_name":"Moldova, Republic of"},{"country_code":"MC", "country_name":"Monaco"},{"country_code":"MN", "country_name":"Mongolia"},{"country_code":"ME", "country_name":"Montenegro"},{"country_code":"MS", "country_name":"Montserrat"},{"country_code":"MA", "country_name":"Morocco"},{"country_code":"MZ", "country_name":"Mozambique"},{"country_code":"MM", "country_name":"Myanmar"},{"country_code":"NA", "country_name":"Namibia"},{"country_code":"NR", "country_name":"Nauru"},{"country_code":"NP", "country_name":"Nepal"},{"country_code":"NL", "country_name":"Netherlands"},{"country_code":"AN", "country_name":"Netherlands Antilles"},{"country_code":"NC", "country_name":"New Caledonia"},{"country_code":"NZ", "country_name":"New Zealand"},{"country_code":"NI", "country_name":"Nicaragua"},{"country_code":"NE", "country_name":"Niger"},{"country_code":"NG", "country_name":"Nigeria"},{"country_code":"NU", "country_name":"Niue"},{"country_code":"NF", "country_name":"Norfolk Island"},{"country_code":"MP", "country_name":"Northern Mariana Islands"},{"country_code":"NO", "country_name":"Norway"},{"country_code":"OM", "country_name":"Oman"},{"country_code":"PK", "country_name":"Pakistan"},{"country_code":"PW", "country_name":"Palau"},{"country_code":"PS", "country_name":"Palestinian Territory, Occupied"},{"country_code":"PA", "country_name":"Panama"},{"country_code":"PG", "country_name":"Papua New Guinea"},{"country_code":"PY", "country_name":"Paraguay"},{"country_code":"PE", "country_name":"Peru"},{"country_code":"PH", "country_name":"Philippines"},{"country_code":"PN", "country_name":"Pitcairn"},{"country_code":"PL", "country_name":"Poland"},{"country_code":"PT", "country_name":"Portugal"},{"country_code":"PR", "country_name":"Puerto Rico"},{"country_code":"QA", "country_name":"Qatar"},{"country_code":"RE", "country_name":"Reunion"},{"country_code":"RO", "country_name":"Romania"},{"country_code":"RU", "country_name":"Russian Federation"},{"country_code":"RW", "country_name":"Rwanda"},{"country_code":"BL", "country_name":"Saint Barthélemy"},{"country_code":"SH", "country_name":"Saint Helena"},{"country_code":"KN", "country_name":"Saint Kitts and Nevis"},{"country_code":"LC", "country_name":"Saint Lucia"},{"country_code":"MF", "country_name":"Saint Martin"},{"country_code":"PM", "country_name":"Saint Pierre and Miquelon"},{"country_code":"VC", "country_name":"Saint Vincent and The Grenadines"},{"country_code":"WS", "country_name":"Samoa"},{"country_code":"SM", "country_name":"San Marino"},{"country_code":"ST", "country_name":"Sao Tome and Principe"},{"country_code":"SA", "country_name":"Saudi Arabia"},{"country_code":"SN", "country_name":"Senegal"},{"country_code":"RS", "country_name":"Serbia"},{"country_code":"SC", "country_name":"Seychelles"},{"country_code":"SL", "country_name":"Sierra Leone"},{"country_code":"SG", "country_name":"Singapore"},{"country_code":"SK", "country_name":"Slovakia"},{"country_code":"SI", "country_name":"Slovenia"},{"country_code":"SB", "country_name":"Solomon Islands"},{"country_code":"SO", "country_name":"Somalia"},{"country_code":"ZA", "country_name":"South Africa"},{"country_code":"GS", "country_name":"South Georgia and The South Sandwich Islands"},{"country_code":"ES", "country_name":"Spain"},{"country_code":"LK", "country_name":"Sri Lanka"},{"country_code":"SD", "country_name":"Sudan"},{"country_code":"SR", "country_name":"Suriname"},{"country_code":"SJ", "country_name":"Svalbard and Jan Mayen"},{"country_code":"SZ", "country_name":"Swaziland"},{"country_code":"SE", "country_name":"Sweden"},{"country_code":"CH", "country_name":"Switzerland"},{"country_code":"SY", "country_name":"Syrian Arab Republic"},{"country_code":"TW", "country_name":"Taiwan"},{"country_code":"TJ", "country_name":"Tajikistan"},{"country_code":"TZ", "country_name":"Tanzania, United Republic of"},{"country_code":"TH", "country_name":"Thailand"},{"country_code":"TL", "country_name":"Timor-Leste"},{"country_code":"TG", "country_name":"Togo"},{"country_code":"TK", "country_name":"Tokelau"},{"country_code":"TO", "country_name":"Tonga"},{"country_code":"TT", "country_name":"Trinidad and Tobago"},{"country_code":"TN", "country_name":"Tunisia"},{"country_code":"TR", "country_name":"Turkey"},{"country_code":"TM", "country_name":"Turkmenistan"},{"country_code":"TC", "country_name":"Turks and Caicos Islands"},{"country_code":"TV", "country_name":"Tuvalu"},{"country_code":"UG", "country_name":"Uganda"},{"country_code":"UA", "country_name":"Ukraine"},{"country_code":"AE", "country_name":"United Arab Emirates"},{"country_code":"GB", "country_name":"United Kingdom"},{"country_code":"UM", "country_name":"United States Minor Outlying Islands"},{"country_code":"US", "country_name":"United States of America"},{"country_code":"UY", "country_name":"Uruguay"},{"country_code":"UZ", "country_name":"Uzbekistan"},{"country_code":"VU", "country_name":"Vanuatu"},{"country_code":"VE", "country_name":"Venezuela"},{"country_code":"VN", "country_name":"Viet Nam"},{"country_code":"VG", "country_name":"Virgin Islands, British"},{"country_code":"VI", "country_name":"Virgin Islands, U.S."},{"country_code":"WF", "country_name":"Wallis and Futuna"},{"country_code":"EH", "country_name":"Western Sahara"},{"country_code":"YE", "country_name":"Yemen"},{"country_code":"ZM", "country_name":"Zambia"},{"country_code":"ZW", "country_name":"Zimbabwe"},{"country_code":"AX", "country_name":"Åland"}]}';
    }
    
    
    public function getDataModifiedDate()
    {
        $this->load->database();
        $query = $this->db->query("select * from manage_version order by id desc limit 1");
        $result = $query->result();
        if(count($result) == 0)
        {
            echo '{"result":1, "message":"data is not ready yet", "time":"0"}';
        }
        else
        {
            echo '{"result":1, "message":"data is ready now", "time":"'.$result[0]->modified_date.'"}';
        }
    }
    
    public function downloadLatestZip()
    {
        $this->load->database();
        $query = $this->db->query("select * from manage_version order by id desc limit 1");
        $result = $query->result();
        if(count($result) == 0)
        {
            echo '{"result":0, "message":"data is not ready yet"}';
            return;
        }
        
        $zipname = $result[0]->modified_date.".omz";
        $zipPath = APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/".$zipname;
        if(!file_exists($zipPath))
        {
            //Delete Directory
            $this->delete_directory(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources_");
            //Copy Image to temp folder
            $this->recurse_copy(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/", APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources_/");
            //Make Zip file
            HZip::zipDir(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources_/", $zipPath);
            //Delete Directory
            //$this->delete_directory(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources_/");
        }
        header('Content-Type: application/stream');
        header('Content-disposition: attachment; filename='.$zipname);
        header('Content-Length: ' . filesize($zipPath));
        readfile($zipPath);
    }
    
    private function delete_directory($dirname) {
        if (is_dir($dirname))
            $dir_handle = opendir($dirname);
        if (!$dir_handle)
             return false;
        while($file = readdir($dir_handle)) {
              if ($file != "." && $file != "..") {
                   if (!is_dir($dirname."/".$file))
                        unlink($dirname."/".$file);
                   else
                        $this->delete_directory($dirname.'/'.$file);
              }
        }
        closedir($dir_handle);
        rmdir($dirname);
        return true;
   }

    private function recurse_copy($src, $dst) {
        $dir = opendir($src);
        @mkdir($dst);
        while(false !== ( $file = readdir($dir)) ) {
            if (( $file != '.' ) && ( $file != '..' )) {
                if ( is_dir($src . '/' . $file) ) {
                    $this->recurse_copy($src . '/' . $file,$dst . '/' . $file);
                }
                else if($this->get_extension ($file) == 'jpg'){
                    //copy($src . '/' . $file,$dst . '/' . $file);
                    $read_file = $src . '/' . $file;
                    $read_handle = fopen($read_file, "rb");
                    $fread_size = filesize($read_file);
                    $contents = fread($read_handle, $fread_size); 
                    fclose($read_handle);
                    
                    $write_file = $dst . '/' . basename($file, ".jpg").".oms";
                    $write_handle = fopen($write_file, "wb");
                    fwrite($write_handle, 0, 1);
                    fwrite($write_handle, $contents, $fread_size);
                    fflush($write_handle);
                    fclose($write_handle);
                }
            }
        }
        closedir($dir);
    }
    
  
    function get_extension($file) {
        return pathinfo($file, PATHINFO_EXTENSION);
    }

    public function prepareData()
    {
        $this->load->database();
        $query = $this->db->query("select * from manage_version order by id desc limit 1");
        $result = $query->result();
        if(count($result) > 0)
        {
            $zipname = $result[0]->modified_date.".omz";
            $zipPath = APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/".$zipname;
            if(file_exists($zipPath))
            {
                unlink($zipPath);
            }
            
            $zipname = $result[0]->modified_date."_project.zip";
            $zipPath = APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/".$zipname;
            if(file_exists($zipPath))
            {
                unlink($zipPath);
            }
            $this->db->query("delete from manage_version");
            
            $this->delete_directory(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/json");
        }
        
        //Prepare Manage Version
        $time = time();
        $this->db->query("insert into manage_version (modified_date) values (".$time.")");
        $query = $this->db->query("select * from manage_version order by id desc limit 1");
        $result = $query->result();
        
        //Create Image Zip File
        $zipname = $result[0]->modified_date.".omz";
        $zipPath = APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/".$zipname;
        
        //Delete Directory
        $this->delete_directory(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources_");
        //Copy Image to temp folder
        $this->recurse_copy(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/", APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources_/");
        //Make Zip file
        HZip::zipDir(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources_", $zipPath);
        //Delete Directory
        $this->delete_directory(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources_");
        
        //Create JSON Zip File
        $zipname = $result[0]->modified_date."_project.zip";
        $zipPath = APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/".$zipname;
        if($this->makeAllProjectContent())
        {
            HZip::zipDir(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/json", $zipPath);
            $this->delete_directory(APP_ROOT_PATH."/web/bundles/swarminfoimagescript/ressources/json");
        }
    }
}

