import { Headline, Popup, Spacer } from "@dco/sdv-ui";

const GraphModal = (props: any) => {
    return <>
        <Popup invert={true} dim show={props.show} onClose={props.onClose} style={{ zIndex: 100 }}>
            <Headline>
                {`Release ${props.releaseId} `}
            </Headline>
            <Spacer />
            
        </Popup>
    </>
}

export default GraphModal